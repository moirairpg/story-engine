package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.List;

public record SearchAdventureLorebookEntriesResult(
        int page,
        int items,
        long totalItems,
        int totalPages,
        List<AdventureLorebookEntryDetails> results) {
}
