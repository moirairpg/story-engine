package me.moirai.storyengine.common.util;

import java.util.function.Function;

public final class Functions {

    public static <I, O> O mapOrNull(I value, Function<I, O> map) {

        if (value == null) {
            return null;
        }

        return map.apply(value);
    }

    public static <I, O> O mapOrDefault(I value, O defaultValue, Function<I, O> map) {

        if (value == null) {
            return defaultValue;
        }

        return map.apply(value);
    }
}
