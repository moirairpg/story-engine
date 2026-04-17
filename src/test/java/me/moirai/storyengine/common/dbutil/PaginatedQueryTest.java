package me.moirai.storyengine.common.dbutil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import me.moirai.storyengine.common.enums.SortDirection;

public class PaginatedQueryTest {

    private static final String BASE_SELECT = "SELECT a.public_id, a.name FROM adventure a";

    @Test
    public void shouldProduceDataSqlWithWhereOrderByLimitAndOffset() {

        // Given
        var query = PaginatedQuery.builder()
                .select(BASE_SELECT)
                .filter(Filters.containsIgnoreCase("a.name", "name", "dragon"))
                .sortBy("a.name", SortDirection.ASC)
                .page(1, 10)
                .build();

        // When
        var sql = query.sql();

        // Then
        assertThat(sql).isEqualTo(
                BASE_SELECT + " WHERE (a.name ILIKE :name) ORDER BY a.name ASC LIMIT :limit OFFSET :offset");
    }

    @Test
    public void shouldProduceCountSqlWrappingBaseQuery() {

        // Given
        var query = PaginatedQuery.builder()
                .select(BASE_SELECT)
                .filter(Filters.equals("a.game_mode", "gameMode", "RPG"))
                .sortBy("a.creation_date", SortDirection.DESC)
                .page(1, 10)
                .build();

        // When
        var countSql = query.countSql();

        // Then
        assertThat(countSql).isEqualTo(
                "SELECT COUNT(*) FROM (" + BASE_SELECT + " WHERE (a.game_mode = :gameMode)) AS count_query");
    }

    @Test
    public void shouldIncludeLimitAndOffsetInDataQueryParameters() {

        // Given
        var query = PaginatedQuery.builder()
                .select(BASE_SELECT)
                .filter(Filters.equals("a.game_mode", "gameMode", "RPG"))
                .sortBy("a.name", SortDirection.ASC)
                .page(2, 15)
                .build();

        // When
        var params = query.parameters();

        // Then
        assertThat(params).containsKey("limit");
        assertThat(params).containsKey("offset");
        assertThat(params.get("limit")).isEqualTo(15);
        assertThat(params.get("offset")).isEqualTo(15);
    }

    @Test
    public void shouldExcludeLimitAndOffsetFromCountParameters() {

        // Given
        var query = PaginatedQuery.builder()
                .select(BASE_SELECT)
                .filter(Filters.equals("a.game_mode", "gameMode", "RPG"))
                .sortBy("a.name", SortDirection.ASC)
                .page(2, 15)
                .build();

        // When
        var countParams = query.countParameters();

        // Then
        assertThat(countParams).doesNotContainKey("limit");
        assertThat(countParams).doesNotContainKey("offset");
        assertThat(countParams).containsEntry("gameMode", "RPG");
    }

    @Test
    public void shouldDefaultToPage1AndSize10WhenNotProvided() {

        // Given
        var query = PaginatedQuery.builder()
                .select(BASE_SELECT)
                .sortBy("a.name", SortDirection.ASC)
                .page(null, null)
                .build();

        // When
        var params = query.parameters();

        // Then
        assertThat(query.page()).isEqualTo(1);
        assertThat(query.size()).isEqualTo(10);
        assertThat(params.get("limit")).isEqualTo(10);
        assertThat(params.get("offset")).isEqualTo(0);
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenSortFieldIsNull() {

        // Given / When / Then
        assertThatThrownBy(() -> PaginatedQuery.builder()
                .select(BASE_SELECT)
                .sortBy(null, SortDirection.ASC)
                .page(1, 10)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sort field is required");
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenSortFieldIsBlank() {

        // Given / When / Then
        assertThatThrownBy(() -> PaginatedQuery.builder()
                .select(BASE_SELECT)
                .sortBy("   ", SortDirection.ASC)
                .page(1, 10)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sort field is required");
    }

    @Test
    public void shouldDefaultToDescWhenDirectionIsNull() {

        // Given
        var query = PaginatedQuery.builder()
                .select(BASE_SELECT)
                .sortBy("a.name", null)
                .page(1, 10)
                .build();

        // When
        var sql = query.sql();

        // Then
        assertThat(sql).contains("ORDER BY a.name DESC");
    }

    @Test
    public void shouldProduceOffset0WhenPageIs1() {

        // Given
        var query = PaginatedQuery.builder()
                .select(BASE_SELECT)
                .sortBy("a.name", SortDirection.ASC)
                .page(1, 20)
                .build();

        // When
        var params = query.parameters();

        // Then
        assertThat(params.get("offset")).isEqualTo(0);
    }

    @Test
    public void shouldProduceOffset40WhenPage3AndSize20() {

        // Given
        var query = PaginatedQuery.builder()
                .select(BASE_SELECT)
                .sortBy("a.name", SortDirection.ASC)
                .page(3, 20)
                .build();

        // When
        var params = query.parameters();

        // Then
        assertThat(params.get("offset")).isEqualTo(40);
    }

    @Test
    public void shouldSkipEmptyOptionalFilters() {

        // Given
        var query = PaginatedQuery.builder()
                .select(BASE_SELECT)
                .filter(Filters.equals("a.name", "name", null))
                .filter(Filters.equals("a.game_mode", "gameMode", "RPG"))
                .sortBy("a.name", SortDirection.ASC)
                .page(1, 10)
                .build();

        // When
        var sql = query.sql();

        // Then
        assertThat(sql).isEqualTo(
                BASE_SELECT + " WHERE (a.game_mode = :gameMode) ORDER BY a.name ASC LIMIT :limit OFFSET :offset");
    }

    @Test
    public void shouldProduceNoWhereClauseWhenAllFiltersAreEmpty() {

        // Given
        var query = PaginatedQuery.builder()
                .select(BASE_SELECT)
                .filter(Filters.equals("a.name", "name", null))
                .sortBy("a.name", SortDirection.ASC)
                .page(1, 10)
                .build();

        // When
        var sql = query.sql();

        // Then
        assertThat(sql).isEqualTo(BASE_SELECT + " ORDER BY a.name ASC LIMIT :limit OFFSET :offset");
    }
}
