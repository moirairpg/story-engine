package me.moirai.storyengine.core.port.inbound.adventure;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdventureLorebookEntryDetails(
        UUID id,
        UUID adventureId,
        String name,
        String regex,
        String description,
        String playerId,
        boolean isPlayerCharacter,
        OffsetDateTime creationDate,
        OffsetDateTime lastUpdateDate) {
}
