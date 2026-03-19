package me.moirai.storyengine.common.testutil;

import static java.util.stream.Collectors.joining;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import me.moirai.storyengine.common.annotation.RandomUuid;

@Component
public class DbTestHelper {

    private final JdbcClient jdbcClient;

    public DbTestHelper(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public <T> void insert(Object value, Class<T> type) {
        validateEntity(type);
        applyUuidGeneration(value, type);
        var tableName = resolveTableName(type);
        var params = buildParamMap(collectFields(type), value);
        var columns = String.join(", ", params.keySet());
        var placeholders = params.keySet().stream()
                .map(k -> ":" + k)
                .collect(joining(", "));
        jdbcClient.sql("INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")")
                .params(params)
                .update();
    }

    public <T> void clearAndInsert(Object value, Class<T> type) {
        clear(type);
        insert(value, type);
    }

    public <T> void update(Object value, Object primaryKeyValue, Class<T> type) {
        validateEntity(type);
        applyUuidGeneration(value, type);
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
                    if (Modifier.isStatic(field.getModifiers())) continue;
                    if (Modifier.isTransient(field.getModifiers())) continue;
                    if (field.isAnnotationPresent(Transient.class)) continue;
                    if (field.isAnnotationPresent(OneToMany.class)) continue;
                    if (field.isAnnotationPresent(ManyToMany.class)) continue;
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
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (Modifier.isTransient(field.getModifiers())) continue;
            if (field.isAnnotationPresent(Transient.class)) continue;
            if (field.isAnnotationPresent(Formula.class)) continue;
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

    private void applyUuidGeneration(Object value, Class<?> type) {
        try {
            for (var field : collectFields(type)) {
                if (!field.isAnnotationPresent(RandomUuid.class)) continue;
                field.setAccessible(true);
                if (field.get(value) != null) continue;
                field.set(value, Generators.timeBasedEpochGenerator().generate());
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Map<String, Object> buildParamMap(List<Field> fields, Object value) {
        try {
            var params = new HashMap<String, Object>();
            for (var field : fields) {
                field.setAccessible(true);
                var fieldValue = field.get(value);
                if (field.isAnnotationPresent(GeneratedValue.class) && fieldValue == null) continue;
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
                    }
                    params.put(resolveColumnName(field), fieldValue);
                }
            }
            return params;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private String toSnakeCase(String name) {
        return name.replaceAll("([A-Z])", "_$1").toLowerCase().replaceFirst("^_", "");
    }
}
