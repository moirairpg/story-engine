package me.moirai.storyengine.common.dbutil;

import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class SqlArrays {

    private SqlArrays() {
    }

    public static <T> List<T> toList(Array array, Class<T> clazz) {

        try {
            var raw = (Object[]) array.getArray();
            var result = new ArrayList<T>(raw.length);

            for (var value : raw) {
                result.add(clazz.cast(value));
            }

            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
