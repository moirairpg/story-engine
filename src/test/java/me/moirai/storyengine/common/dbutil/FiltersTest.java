package me.moirai.storyengine.common.dbutil;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

public class FiltersTest {

    @Test
    public void shouldReturnFilterWhenEqualsValueIsPresent() {

        // Given
        var column = "a.status";
        var param = "status";
        var value = "ACTIVE";

        // When
        var result = Filters.equals(column, param, value);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().clause()).isEqualTo("a.status = :status");
        assertThat(result.get().paramName()).isEqualTo("status");
        assertThat(result.get().value()).isEqualTo("ACTIVE");
    }

    @Test
    public void shouldReturnEmptyWhenEqualsValueIsNull() {

        // Given / When
        var result = Filters.equals("a.status", "status", null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnFilterWhenEqualsIgnoreCaseValueIsPresent() {

        // Given
        var column = "a.name";
        var param = "name";
        var value = "Hero";

        // When
        var result = Filters.equalsIgnoreCase(column, param, value);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().clause()).isEqualTo("LOWER(a.name) = LOWER(:name)");
        assertThat(result.get().paramName()).isEqualTo("name");
        assertThat(result.get().value()).isEqualTo("Hero");
    }

    @Test
    public void shouldReturnEmptyWhenEqualsIgnoreCaseValueIsNull() {

        // Given / When
        var result = Filters.equalsIgnoreCase("a.name", "name", null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnEmptyWhenEqualsIgnoreCaseValueIsBlank() {

        // Given / When
        var result = Filters.equalsIgnoreCase("a.name", "name", "   ");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnFilterWithTrailingWildcardWhenStartsWithValueIsPresent() {

        // Given
        var column = "a.name";
        var param = "name";
        var value = "drag";

        // When
        var result = Filters.startsWith(column, param, value);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().clause()).isEqualTo("a.name LIKE :name");
        assertThat(result.get().value()).isEqualTo("drag%");
    }

    @Test
    public void shouldReturnEmptyWhenStartsWithValueIsBlank() {

        // Given / When
        var result = Filters.startsWith("a.name", "name", "");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnFilterWithIlikeAndTrailingWildcardWhenStartsWithIgnoreCaseValueIsPresent() {

        // Given
        var column = "a.name";
        var param = "name";
        var value = "drag";

        // When
        var result = Filters.startsWithIgnoreCase(column, param, value);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().clause()).isEqualTo("a.name ILIKE :name");
        assertThat(result.get().value()).isEqualTo("drag%");
    }

    @Test
    public void shouldReturnEmptyWhenStartsWithIgnoreCaseValueIsBlank() {

        // Given / When
        var result = Filters.startsWithIgnoreCase("a.name", "name", null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnFilterWithLeadingWildcardWhenEndsWithValueIsPresent() {

        // Given
        var column = "a.name";
        var param = "name";
        var value = "on";

        // When
        var result = Filters.endsWith(column, param, value);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().clause()).isEqualTo("a.name LIKE :name");
        assertThat(result.get().value()).isEqualTo("%on");
    }

    @Test
    public void shouldReturnEmptyWhenEndsWithValueIsBlank() {

        // Given / When
        var result = Filters.endsWith("a.name", "name", "");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnFilterWithIlikeAndLeadingWildcardWhenEndsWithIgnoreCaseValueIsPresent() {

        // Given
        var column = "a.name";
        var param = "name";
        var value = "on";

        // When
        var result = Filters.endsWithIgnoreCase(column, param, value);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().clause()).isEqualTo("a.name ILIKE :name");
        assertThat(result.get().value()).isEqualTo("%on");
    }

    @Test
    public void shouldReturnEmptyWhenEndsWithIgnoreCaseValueIsBlank() {

        // Given / When
        var result = Filters.endsWithIgnoreCase("a.name", "name", null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnFilterWithWrappedWildcardsWhenContainsValueIsPresent() {

        // Given
        var column = "a.name";
        var param = "name";
        var value = "rago";

        // When
        var result = Filters.contains(column, param, value);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().clause()).isEqualTo("a.name LIKE :name");
        assertThat(result.get().value()).isEqualTo("%rago%");
    }

    @Test
    public void shouldReturnEmptyWhenContainsValueIsBlank() {

        // Given / When
        var result = Filters.contains("a.name", "name", "");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnFilterWithIlikeAndWrappedWildcardsWhenContainsIgnoreCaseValueIsPresent() {

        // Given
        var column = "a.name";
        var param = "name";
        var value = "rago";

        // When
        var result = Filters.containsIgnoreCase(column, param, value);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().clause()).isEqualTo("a.name ILIKE :name");
        assertThat(result.get().value()).isEqualTo("%rago%");
    }

    @Test
    public void shouldReturnEmptyWhenContainsIgnoreCaseValueIsBlank() {

        // Given / When
        var result = Filters.containsIgnoreCase("a.name", "name", null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnFilterWhenGreaterThanValueIsPresent() {

        // Given / When
        var result = Filters.greaterThan("a.level", "level", 5);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().clause()).isEqualTo("a.level > :level");
        assertThat(result.get().value()).isEqualTo(5);
    }

    @Test
    public void shouldReturnEmptyWhenGreaterThanValueIsNull() {

        // Given / When
        var result = Filters.greaterThan("a.level", "level", null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnFilterWhenGreaterOrEqualThanValueIsPresent() {

        // Given / When
        var result = Filters.greaterOrEqualThan("a.level", "level", 5);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().clause()).isEqualTo("a.level >= :level");
        assertThat(result.get().value()).isEqualTo(5);
    }

    @Test
    public void shouldReturnEmptyWhenGreaterOrEqualThanValueIsNull() {

        // Given / When
        var result = Filters.greaterOrEqualThan("a.level", "level", null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnFilterWhenLowerThanValueIsPresent() {

        // Given / When
        var result = Filters.lowerThan("a.level", "level", 10);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().clause()).isEqualTo("a.level < :level");
        assertThat(result.get().value()).isEqualTo(10);
    }

    @Test
    public void shouldReturnEmptyWhenLowerThanValueIsNull() {

        // Given / When
        var result = Filters.lowerThan("a.level", "level", null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnFilterWhenLowerOrEqualThanValueIsPresent() {

        // Given / When
        var result = Filters.lowerOrEqualThan("a.level", "level", 10);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().clause()).isEqualTo("a.level <= :level");
        assertThat(result.get().value()).isEqualTo(10);
    }

    @Test
    public void shouldReturnEmptyWhenLowerOrEqualThanValueIsNull() {

        // Given / When
        var result = Filters.lowerOrEqualThan("a.level", "level", null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnFilterWhenInCollectionIsPresent() {

        // Given
        var values = List.of("RPG", "ADVENTURE");

        // When
        var result = Filters.in("a.game_mode", "gameMode", values);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().clause()).isEqualTo("a.game_mode IN (:gameMode)");
        assertThat(result.get().value()).isEqualTo(values);
    }

    @Test
    public void shouldReturnEmptyWhenInCollectionIsNull() {

        // Given / When
        var result = Filters.in("a.game_mode", "gameMode", null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnEmptyWhenInCollectionIsEmpty() {

        // Given / When
        var result = Filters.in("a.game_mode", "gameMode", List.of());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnFilterWhenNotInCollectionIsPresent() {

        // Given
        var values = List.of("BANNED");

        // When
        var result = Filters.notIn("a.status", "status", values);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().clause()).isEqualTo("a.status NOT IN (:status)");
        assertThat(result.get().value()).isEqualTo(values);
    }

    @Test
    public void shouldReturnEmptyWhenNotInCollectionIsNull() {

        // Given / When
        var result = Filters.notIn("a.status", "status", null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnEmptyWhenNotInCollectionIsEmpty() {

        // Given / When
        var result = Filters.notIn("a.status", "status", List.of());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnLiteralTrueClauseWithNoParameterWhenIsTrue() {

        // Given / When
        var result = Filters.isTrue("a.active");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().clause()).isEqualTo("a.active = TRUE");
        assertThat(result.get().paramName()).isNull();
        assertThat(result.get().value()).isNull();
    }

    @Test
    public void shouldReturnLiteralFalseClauseWithNoParameterWhenIsFalse() {

        // Given / When
        var result = Filters.isFalse("a.active");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().clause()).isEqualTo("a.active = FALSE");
        assertThat(result.get().paramName()).isNull();
        assertThat(result.get().value()).isNull();
    }
}
