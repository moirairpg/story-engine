package me.moirai.storyengine.core.port.inbound.world;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WorldLorebookEntryDetails(
        UUID id,
        UUID worldId,
        String name,
        String regex,
        String description,
        OffsetDateTime creationDate,
        OffsetDateTime lastUpdateDate) {
}
