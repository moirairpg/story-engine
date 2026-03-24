package me.moirai.storyengine.core.port.inbound.adventure;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

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
        String ownerId,
        boolean isMultiplayer,
        Instant creationDate,
        Instant lastUpdateDate,
        ModelConfigurationDto modelConfiguration,
        ContextAttributesDto contextAttributes,
        Set<String> usersAllowedToRead,
        Set<String> usersAllowedToWrite) {

    public AdventureDetails {
        usersAllowedToRead = Set.copyOf(usersAllowedToRead);
        usersAllowedToWrite = Set.copyOf(usersAllowedToWrite);
    }
}
