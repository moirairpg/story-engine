package me.moirai.storyengine.core.port.inbound.adventure;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import me.moirai.storyengine.common.domain.Permission;

public record AdventureDetails(
        UUID id,
        String name,
        String description,
        String adventureStart,
        UUID worldId,
        UUID personaId,
        String channelId,
        String visibility,
        String moderation,
        String gameMode,
        boolean isMultiplayer,
        Instant creationDate,
        Instant lastUpdateDate,
        ModelConfigurationDto modelConfiguration,
        ContextAttributesDto contextAttributes,
        Set<Permission> permissions) {

    public AdventureDetails {
        permissions = Set.copyOf(permissions);
    }
}
