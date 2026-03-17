package me.moirai.storyengine.core.port.inbound.world;

import java.util.List;

public record SearchWorldLorebookEntriesResult(
        int page,
        int items,
        long totalItems,
        int totalPages,
        List<WorldLorebookEntryDetails> results) {
}
