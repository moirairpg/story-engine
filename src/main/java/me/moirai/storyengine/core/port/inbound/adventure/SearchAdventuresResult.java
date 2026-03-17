package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.List;

public record SearchAdventuresResult(
        int page,
        int totalPages,
        int items,
        long totalItems,
        List<AdventureDetails> results) {
}
