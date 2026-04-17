package me.moirai.storyengine.core.port.inbound.world;

import java.time.Instant;
import java.util.UUID;

public record WorldLorebookEntryDetails(
        UUID id,
        UUID worldId,
        String name,
        String description,
        Instant creationDate,
        Instant lastUpdateDate) {
}
