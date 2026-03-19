package me.moirai.storyengine.common.dbutil;

import java.util.Collection;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

public final class Filters {

    private Filters() {
    }

    public static Optional<Filter> equals(String column, String param, Object value) {

        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(new Filter(column + " = :" + param, param, value));
    }

    public static Optional<Filter> equalsIgnoreCase(String column, String param, String value) {

        if (isBlank(value)) {
            return Optional.empty();
        }

        return Optional.of(new Filter(
                "LOWER(" + column + ") = LOWER(:" + param + ")",
                param, value));
    }

    public static Optional<Filter> startsWith(String column, String param, String value) {

        if (isBlank(value)) {
            return Optional.empty();
        }

        return Optional.of(new Filter(column + " LIKE :" + param, param, value + "%"));
    }

    public static Optional<Filter> startsWithIgnoreCase(String column, String param, String value) {

        if (isBlank(value)) {
            return Optional.empty();
        }

        return Optional.of(new Filter(column + " ILIKE :" + param, param, value + "%"));
    }

    public static Optional<Filter> endsWith(String column, String param, String value) {

        if (isBlank(value)) {
            return Optional.empty();
        }

        return Optional.of(new Filter(column + " LIKE :" + param, param, "%" + value));
    }

    public static Optional<Filter> endsWithIgnoreCase(String column, String param, String value) {

        if (isBlank(value)) {
            return Optional.empty();
        }

        return Optional.of(new Filter(column + " ILIKE :" + param, param, "%" + value));
    }

    public static Optional<Filter> contains(String column, String param, String value) {

        if (isBlank(value)) {
            return Optional.empty();
        }

        return Optional.of(new Filter(column + " LIKE :" + param, param, "%" + value + "%"));
    }

    public static Optional<Filter> containsIgnoreCase(String column, String param, String value) {

        if (isBlank(value)) {
            return Optional.empty();
        }

        return Optional.of(new Filter(column + " ILIKE :" + param, param, "%" + value + "%"));
    }

    public static Optional<Filter> greaterThan(String column, String param, Comparable<?> value) {

        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(new Filter(column + " > :" + param, param, value));
    }

    public static Optional<Filter> greaterOrEqualThan(String column, String param, Comparable<?> value) {

        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(new Filter(column + " >= :" + param, param, value));
    }

    public static Optional<Filter> lowerThan(String column, String param, Comparable<?> value) {

        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(new Filter(column + " < :" + param, param, value));
    }

    public static Optional<Filter> lowerOrEqualThan(String column, String param, Comparable<?> value) {

        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(new Filter(column + " <= :" + param, param, value));
    }

    public static Optional<Filter> in(String column, String param, Collection<?> values) {

        if (values == null || values.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new Filter(column + " IN (:" + param + ")", param, values));
    }

    public static Optional<Filter> notIn(String column, String param, Collection<?> values) {

        if (values == null || values.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new Filter(column + " NOT IN (:" + param + ")", param, values));
    }

    public static Optional<Filter> isTrue(String column) {
        return Optional.of(new Filter(column + " = TRUE", null, null));
    }

    public static Optional<Filter> isFalse(String column) {
        return Optional.of(new Filter(column + " = FALSE", null, null));
    }
}
