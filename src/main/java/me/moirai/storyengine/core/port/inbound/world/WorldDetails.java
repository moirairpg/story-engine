package me.moirai.storyengine.core.port.inbound.world;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import me.moirai.storyengine.common.dto.PermissionDto;

public record WorldDetails(
        UUID id,
        String name,
        String description,
        String adventureStart,
        String narratorName,
        String narratorPersonality,
        String visibility,
        String imageUrl,
        Set<PermissionDto> permissions,
        Set<WorldLorebookEntryDetails> lorebook,
        Instant creationDate,
        Instant lastUpdateDate) {

    public WorldDetails {
        permissions = Set.copyOf(permissions);
        lorebook = Set.copyOf(lorebook);
    }
}
