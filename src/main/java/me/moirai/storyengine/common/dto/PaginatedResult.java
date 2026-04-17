package me.moirai.storyengine.common.dto;

import java.util.Collections;
import java.util.List;

public record PaginatedResult<T>(
        List<T> data,
        long items,
        long totalItems,
        int page,
        int totalPages) {

    public PaginatedResult {
        data = Collections.unmodifiableList(data);
    }

    public static <T> PaginatedResult<T> of(List<T> data, long totalItems, int page, int pageSize) {

        var totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));

        return new PaginatedResult<>(data, data.size(), totalItems, page, totalPages);
    }
}
