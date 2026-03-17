package me.moirai.storyengine.core.port.inbound.world;

import java.util.List;

public record SearchWorldsResult(
        int page,
        int items,
        long totalItems,
        int totalPages,
        List<WorldDetails> results) {
}