package me.moirai.storyengine.core.port.inbound.adventure;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.common.enums.Visibility;

public record AdventureDetails(
        UUID id,
        String name,
        String description,
        String adventureStart,
        UUID worldId,
        UUID personaId,
        Visibility visibility,
        Moderation moderation,
        boolean isMultiplayer,
        Instant creationDate,
        Instant lastUpdateDate,
        ModelConfigurationDto modelConfiguration,
        ContextAttributesDto contextAttributes,
        Set<PermissionDto> permissions,
        Set<AdventureLorebookEntryDetails> lorebook) {

    public AdventureDetails {
        permissions = Set.copyOf(permissions);
        lorebook = Set.copyOf(lorebook);
    }
}
