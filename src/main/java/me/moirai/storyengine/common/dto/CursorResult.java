package me.moirai.storyengine.common.dto;

import java.util.Collections;
import java.util.List;

public record CursorResult<T>(
        List<T> data,
        boolean hasMore) {

    public CursorResult {
        data = Collections.unmodifiableList(data);
    }

    public static <T> CursorResult<T> of(List<T> data, int pageSize) {
        return new CursorResult<>(data, data.size() == pageSize);
    }
}
