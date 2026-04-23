package me.moirai.storyengine.common.dbutil;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class QueryBuilderTest {

    private static final String BASE_SELECT = "SELECT w.public_id, w.name FROM world w";

    @Test
    public void shouldProduceSelectWithNoWhereWhenAllFiltersAreEmpty() {

        // Given
        var query = QueryBuilder.select(BASE_SELECT)
                .filter(Filters.containsIgnoreCase("w.name", "name", null))
                .filter(Filters.equals("w.owner_id", "ownerId", null))
                .build();

        // When
        var sql = query.sql();

        // Then
        assertThat(sql).isEqualTo(BASE_SELECT);
    }

    @Test
    public void shouldProduceSelectWithWhereWhenSingleFilterIsPresent() {

        // Given
        var query = QueryBuilder.select(BASE_SELECT)
                .filter(Filters.equals("w.public_id", "id", "abc-123"))
                .build();

        // When
        var sql = query.sql();

        // Then
        assertThat(sql).isEqualTo(BASE_SELECT + " WHERE (w.public_id = :id)");
    }

    @Test
    public void shouldProduceSelectWithMultipleFiltersJoinedByAnd() {

        // Given
        var query = QueryBuilder.select(BASE_SELECT)
                .filter(Filters.containsIgnoreCase("w.name", "name", "forest"))
                .filter(Filters.equals("w.visibility", "visibility", "PUBLIC"))
                .build();

        // When
        var sql = query.sql();

        // Then
        assertThat(sql).isEqualTo(
                BASE_SELECT + " WHERE (w.name ILIKE :name) AND (w.visibility = :visibility)");
    }

    @Test
    public void shouldSkipEmptyOptionalFilters() {

        // Given
        var query = QueryBuilder.select(BASE_SELECT)
                .filter(Optional.empty())
                .filter(Filters.equals("w.visibility", "visibility", "PUBLIC"))
                .build();

        // When
        var sql = query.sql();

        // Then
        assertThat(sql).isEqualTo(BASE_SELECT + " WHERE (w.visibility = :visibility)");
    }

    @Test
    public void shouldContainOnlyNonNullBindingsInParametersMap() {

        // Given
        var query = QueryBuilder.select(BASE_SELECT)
                .filter(Filters.containsIgnoreCase("w.name", "name", "forest"))
                .filter(Filters.equals("w.visibility", "visibility", "PUBLIC"))
                .build();

        // When
        var params = query.parameters();

        // Then
        assertThat(params).containsOnlyKeys("name", "visibility");
        assertThat(params.get("name")).isEqualTo("%forest%");
        assertThat(params.get("visibility")).isEqualTo("PUBLIC");
    }

    @Test
    public void shouldReturnEmptyParametersMapWhenAllFiltersAreSkipped() {

        // Given
        var query = QueryBuilder.select(BASE_SELECT)
                .filter(Filters.equals("w.name", "name", null))
                .build();

        // When
        var params = query.parameters();

        // Then
        assertThat(params).isEmpty();
    }

    @Test
    public void shouldNotAddEntryToParametersMapForFiltersWithNoParamName() {

        // Given
        var query = QueryBuilder.select(BASE_SELECT)
                .filter(Filters.isTrue("w.active"))
                .build();

        // When
        var sql = query.sql();
        var params = query.parameters();

        // Then
        assertThat(sql).isEqualTo(BASE_SELECT + " WHERE (w.active = TRUE)");
        assertThat(params).isEmpty();
    }
}
