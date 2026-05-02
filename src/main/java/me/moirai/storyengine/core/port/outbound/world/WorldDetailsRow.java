package me.moirai.storyengine.core.port.outbound.world;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;

public record WorldDetailsRow(
        UUID id,
        String name,
        String description,
        String adventureStart,
        String narratorName,
        String narratorPersonality,
        String visibility,
        String imageKey,
        Set<PermissionDto> permissions,
        Set<WorldLorebookEntryDetails> lorebook,
        Instant creationDate,
        Instant lastUpdateDate,
        Double uiImagePositionX,
        Double uiImagePositionY) {

    public WorldDetailsRow {
        permissions = Set.copyOf(permissions);
        lorebook = Set.copyOf(lorebook);
    }
}
