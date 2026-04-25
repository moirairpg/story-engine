package me.moirai.storyengine.common.testutil;

import static java.util.stream.Collectors.joining;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.Formula;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import com.fasterxml.uuid.Generators;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import me.moirai.storyengine.common.annotation.RandomUuid;

@Component
public class DbTestHelper {

    private final JdbcClient jdbcClient;

    public DbTestHelper(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @SuppressWarnings("unchecked")
    public <T> T insert(Object value, Class<T> type) {

        validateEntity(type);
        applyUuidGeneration(value, type);
        applyEntityListeners(value, type);

        var tableName = resolveTableName(type);
        var params = buildParamMap(collectFields(type), value);
        var columns = String.join(", ", params.keySet());
        var idField = resolveIdField(type);
        var idColumn = resolveColumnName(idField);
        var placeholders = params.keySet().stream()
                .map(k -> ":" + k)
                .collect(joining(", "));

        var generatedId = jdbcClient
                .sql("INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ") RETURNING "
                        + idColumn)
                .params(params)
                .query(Long.class)
                .single();

        updateWithGeneratedId(value, idField, generatedId);
        insertOneToManyChildren(value, type, generatedId);
        insertElementCollections(value, type, generatedId);

        return (T) value;
    }

    public <T> T clearAndInsert(Object value, Class<T> type) {
        clear(type);
        return insert(value, type);
    }

    public <T> void update(Object value, Long primaryKeyValue, Class<T> type) {

        validateEntity(type);
        applyUuidGeneration(value, type);
        applyEntityListeners(value, type);

        var tableName = resolveTableName(type);
        var idField = resolveIdField(type);
        var idColumn = resolveColumnName(idField);
        var allFields = collectFields(type);
        var nonIdFields = allFields.stream()
                .filter(f -> !f.equals(idField))
                .toList();

        var params = new HashMap<>(buildParamMap(nonIdFields, value));
        params.put("pkValue", primaryKeyValue);

        var setClauses = params.keySet().stream()
                .filter(k -> !k.equals("pkValue"))
                .map(k -> k + " = :" + k)
                .collect(joining(", "));

        jdbcClient.sql("UPDATE " + tableName + " SET " + setClauses + " WHERE " + idColumn + " = :pkValue")
                .params(params)
                .update();

        replaceOneToManyChildren(value, type, primaryKeyValue);
        replaceElementCollections(value, type, primaryKeyValue);
    }

    public <T> void clear(Class<T> type) {
        validateEntity(type);
        jdbcClient.sql("TRUNCATE TABLE " + resolveTableName(type) + " RESTART IDENTITY CASCADE").update();
    }

    public void clearDatabase() {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

        var tableNames = scanner.findCandidateComponents("me.moirai.storyengine")
                .stream()
                .map(bd -> {
                    try {
                        return Class.forName(bd.getBeanClassName());
                    } catch (ClassNotFoundException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .map(this::resolveTableName)
                .collect(joining(", "));

        jdbcClient.sql("TRUNCATE TABLE " + tableNames + " RESTART IDENTITY CASCADE").update();
    }

    private void validateEntity(Class<?> type) {
        if (!type.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("Class " + type.getName() + " is not a JPA @Entity");
        }
    }

    private String resolveTableName(Class<?> type) {
        var table = type.getAnnotation(Table.class);
        if (table != null && !table.name().isBlank()) {
            return table.name();
        }

        return toSnakeCase(type.getSimpleName());
    }

    private List<Field> collectFields(Class<?> type) {
        var result = new ArrayList<Field>();
        var current = type;

        while (current != null && current != Object.class) {
            if (current == type || current.isAnnotationPresent(MappedSuperclass.class)) {
                for (var field : current.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers()))
                        continue;

                    if (Modifier.isTransient(field.getModifiers()))
                        continue;

                    if (field.isAnnotationPresent(Transient.class))
                        continue;

                    if (field.isAnnotationPresent(OneToMany.class))
                        continue;

                    if (field.isAnnotationPresent(ManyToMany.class))
                        continue;

                    if (field.isAnnotationPresent(ElementCollection.class))
                        continue;
                    result.add(field);
                }
            }

            current = current.getSuperclass();
        }

        return result;
    }

    private List<Field> collectEmbeddedFields(Field embeddedField) {
        var result = new ArrayList<Field>();
        var embeddableType = embeddedField.getType();
        for (var field : embeddableType.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()))
                continue;

            if (Modifier.isTransient(field.getModifiers()))
                continue;

            if (field.isAnnotationPresent(Transient.class))
                continue;

            if (field.isAnnotationPresent(Formula.class))
                continue;

            if (field.isAnnotationPresent(ElementCollection.class))
                continue;

            result.add(field);
        }

        return result;
    }

    private Field resolveIdField(Class<?> type) {
        for (var field : collectFields(type)) {
            if (field.isAnnotationPresent(Id.class)) {
                return field;
            }
        }

        throw new IllegalStateException("No @Id field found on " + type.getName());
    }

    private String resolveColumnName(Field field) {
        var column = field.getAnnotation(Column.class);
        if (column != null && !column.name().isBlank()) {
            return column.name();
        }

        return toSnakeCase(field.getName());
    }

    private void updateWithGeneratedId(Object value, Field idField, Long generatedId) {
        try {
            idField.setAccessible(true);
            idField.set(value, generatedId);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private void applyUuidGeneration(Object value, Class<?> type) {
        try {
            for (var field : collectFields(type)) {
                if (!field.isAnnotationPresent(RandomUuid.class))
                    continue;

                field.setAccessible(true);
                if (field.get(value) != null)
                    continue;

                field.set(value, Generators.timeBasedEpochGenerator().generate());
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private void insertOneToManyChildren(Object parent, Class<?> parentType, Long parentId) {
        try {
            for (var field : collectOneToManyFields(parentType)) {
                field.setAccessible(true);
                var collection = (Iterable<?>) field.get(parent);
                if (collection == null)
                    continue;

                var fkColumnName = field.getAnnotation(JoinColumn.class).name();

                for (var child : collection) {
                    var childType = child.getClass();
                    applyUuidGeneration(child, childType);
                    applyEntityListeners(child, childType);

                    var tableName = resolveTableName(childType);
                    var params = new HashMap<>(buildParamMap(collectFields(childType), child));
                    params.put(fkColumnName, parentId);

                    var columns = String.join(", ", params.keySet());
                    var placeholders = params.keySet().stream().map(k -> ":" + k).collect(joining(", "));
                    var idField = resolveIdField(childType);
                    var idColumn = resolveColumnName(idField);
                    var generatedChildId = jdbcClient
                            .sql("INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders
                                    + ") RETURNING " + idColumn)
                            .params(params)
                            .query(Long.class)
                            .single();

                    updateWithGeneratedId(child, idField, generatedChildId);
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<Field> collectOneToManyFields(Class<?> type) {

        var result = new ArrayList<Field>();
        var current = type;
        while (current != null && current != Object.class) {
            for (var field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(OneToMany.class) && field.isAnnotationPresent(JoinColumn.class)) {
                    result.add(field);
                }
            }

            current = current.getSuperclass();
        }

        return result;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Map<String, Object> buildParamMap(List<Field> fields, Object value) {
        try {
            var params = new HashMap<String, Object>();
            for (var field : fields) {
                field.setAccessible(true);
                var fieldValue = field.get(value);

                if (field.isAnnotationPresent(GeneratedValue.class) && fieldValue == null)
                    continue;

                if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)) {
                    var joinColumn = field.getAnnotation(JoinColumn.class);
                    var columnName = (joinColumn != null && !joinColumn.name().isBlank())
                            ? joinColumn.name()
                            : toSnakeCase(field.getName()) + "_id";
                    if (fieldValue != null) {
                        var relatedIdField = resolveIdField(fieldValue.getClass());
                        relatedIdField.setAccessible(true);
                        params.put(columnName, relatedIdField.get(fieldValue));
                    } else {
                        params.put(columnName, null);
                    }
                } else if (field.getType().isAnnotationPresent(Embeddable.class)) {
                    for (var embeddedField : collectEmbeddedFields(field)) {
                        embeddedField.setAccessible(true);
                        var embeddedValue = fieldValue != null ? embeddedField.get(fieldValue) : null;
                        if (embeddedField.isAnnotationPresent(Convert.class) && embeddedValue != null) {
                            var converterClass = embeddedField.getAnnotation(Convert.class).converter();
                            var converter = (AttributeConverter) converterClass.getDeclaredConstructor().newInstance();
                            embeddedValue = converter.convertToDatabaseColumn(embeddedValue);
                        } else if (embeddedValue instanceof Enum<?> enumValue) {
                            embeddedValue = enumValue.name();
                        }
                        params.put(resolveColumnName(embeddedField), embeddedValue);
                    }
                } else {
                    if (field.isAnnotationPresent(Convert.class) && fieldValue != null) {
                        var converterClass = field.getAnnotation(Convert.class).converter();
                        var converter = (AttributeConverter) converterClass.getDeclaredConstructor().newInstance();
                        fieldValue = converter.convertToDatabaseColumn(fieldValue);
                    } else if (fieldValue instanceof Enum<?> enumValue) {
                        fieldValue = enumValue.name();
                    } else if (fieldValue instanceof Instant instant) {
                        fieldValue = OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
                    }
                    params.put(resolveColumnName(field), fieldValue);
                }
            }
            return params;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private void insertElementCollections(Object entity, Class<?> entityType, Long parentId) {
        try {
            var current = entityType;
            while (current != null && current != Object.class) {
                if (current == entityType || current.isAnnotationPresent(MappedSuperclass.class)) {
                    for (var field : current.getDeclaredFields()) {
                        field.setAccessible(true);
                        if (field.isAnnotationPresent(ElementCollection.class)) {
                            insertElementCollection(field, field.get(entity), parentId);
                        } else if (field.getType().isAnnotationPresent(Embeddable.class)) {
                            var embeddedValue = field.get(entity);
                            if (embeddedValue != null) {
                                for (var embeddedField : field.getType().getDeclaredFields()) {
                                    embeddedField.setAccessible(true);
                                    if (embeddedField.isAnnotationPresent(ElementCollection.class)) {
                                        insertElementCollection(embeddedField, embeddedField.get(embeddedValue), parentId);
                                    }
                                }
                            }
                        }
                    }
                }
                current = current.getSuperclass();
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private void insertElementCollection(Field field, Object collectionValue, Long parentId) {
        if (collectionValue == null)
            return;

        var collectionTable = field.getAnnotation(CollectionTable.class);
        if (collectionTable == null)
            return;

        var tableName = collectionTable.name();
        var fkColumnName = collectionTable.joinColumns()[0].name();
        var valueColumn = field.getAnnotation(Column.class);
        var valueColumnName = (valueColumn != null && !valueColumn.name().isBlank()) ? valueColumn.name() : "value";

        if (collectionValue instanceof Map<?, ?> map) {
            var mapKeyColumn = field.getAnnotation(MapKeyColumn.class);
            var keyColumnName = (mapKeyColumn != null && !mapKeyColumn.name().isBlank()) ? mapKeyColumn.name() : "key";
            for (var entry : map.entrySet()) {
                jdbcClient
                        .sql("INSERT INTO " + tableName + " (" + fkColumnName + ", " + keyColumnName + ", "
                                + valueColumnName + ") VALUES (:fk, :key, :val)")
                        .param("fk", parentId)
                        .param("key", entry.getKey())
                        .param("val", entry.getValue())
                        .update();
            }
        } else if (collectionValue instanceof Iterable<?> iterable) {
            try {
                for (var element : iterable) {
                    if (element != null && element.getClass().isAnnotationPresent(Embeddable.class)) {
                        var embeddableParams = new HashMap<String, Object>();
                        embeddableParams.put(fkColumnName, parentId);
                        for (var embeddedField : element.getClass().getDeclaredFields()) {
                            if (Modifier.isStatic(embeddedField.getModifiers()))
                                continue;
                            embeddedField.setAccessible(true);
                            var embeddedValue = embeddedField.get(element);
                            if (embeddedValue instanceof Enum<?> enumValue) {
                                embeddedValue = enumValue.name();
                            }
                            embeddableParams.put(resolveColumnName(embeddedField), embeddedValue);
                        }
                        var embeddableColumns = String.join(", ", embeddableParams.keySet());
                        var embeddablePlaceholders = embeddableParams.keySet().stream()
                                .map(k -> ":" + k).collect(joining(", "));
                        jdbcClient.sql("INSERT INTO " + tableName + " (" + embeddableColumns + ") VALUES ("
                                + embeddablePlaceholders + ")")
                                .params(embeddableParams)
                                .update();
                    } else {
                        jdbcClient
                                .sql("INSERT INTO " + tableName + " (" + fkColumnName + ", " + valueColumnName
                                        + ") VALUES (:fk, :val)")
                                .param("fk", parentId)
                                .param("val", element)
                                .update();
                    }
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private void replaceElementCollections(Object entity, Class<?> entityType, Long parentId) {
        try {
            var current = entityType;
            while (current != null && current != Object.class) {
                if (current == entityType || current.isAnnotationPresent(MappedSuperclass.class)) {
                    for (var field : current.getDeclaredFields()) {
                        field.setAccessible(true);
                        if (field.isAnnotationPresent(ElementCollection.class)) {
                            deleteElementCollection(field, parentId);
                            insertElementCollection(field, field.get(entity), parentId);
                        } else if (field.getType().isAnnotationPresent(Embeddable.class)) {
                            var embeddedValue = field.get(entity);
                            for (var embeddedField : field.getType().getDeclaredFields()) {
                                embeddedField.setAccessible(true);
                                if (embeddedField.isAnnotationPresent(ElementCollection.class)) {
                                    deleteElementCollection(embeddedField, parentId);
                                    insertElementCollection(embeddedField,
                                            embeddedValue != null ? embeddedField.get(embeddedValue) : null, parentId);
                                }
                            }
                        }
                    }
                }
                current = current.getSuperclass();
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private void deleteElementCollection(Field field, Long parentId) {
        var collectionTable = field.getAnnotation(CollectionTable.class);
        if (collectionTable == null)
            return;

        var tableName = collectionTable.name();
        var fkColumnName = collectionTable.joinColumns()[0].name();
        jdbcClient.sql("DELETE FROM " + tableName + " WHERE " + fkColumnName + " = :parentId")
                .param("parentId", parentId)
                .update();
    }

    private void replaceOneToManyChildren(Object parent, Class<?> parentType, Long parentId) {
        for (var field : collectOneToManyFields(parentType)) {
            var fkColumnName = field.getAnnotation(JoinColumn.class).name();
            var childType = (Class<?>) ((java.lang.reflect.ParameterizedType) field.getGenericType())
                    .getActualTypeArguments()[0];
            jdbcClient.sql("DELETE FROM " + resolveTableName(childType) + " WHERE " + fkColumnName + " = :parentId")
                    .param("parentId", parentId)
                    .update();
        }
        insertOneToManyChildren(parent, parentType, parentId);
    }

    private void applyEntityListeners(Object value, Class<?> type) {
        var current = type;
        while (current != null && current != Object.class) {
            var listeners = current.getAnnotation(EntityListeners.class);
            if (listeners != null) {
                for (var listenerClass : listeners.value()) {
                    for (var method : listenerClass.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(PrePersist.class)) {
                            try {
                                var listener = listenerClass.getDeclaredConstructor().newInstance();
                                method.setAccessible(true);
                                method.invoke(listener, value);
                            } catch (ReflectiveOperationException e) {
                                throw new IllegalStateException(e);
                            }
                        }
                    }
                }
            }
            current = current.getSuperclass();
        }
    }

    private String toSnakeCase(String name) {
        return name.replaceAll("([A-Z])", "_$1").toLowerCase().replaceFirst("^_", "");
    }
}
