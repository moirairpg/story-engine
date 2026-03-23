package me.moirai.storyengine.core.port.inbound.world;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record WorldDetails(
        UUID id,
        String name,
        String description,
        String adventureStart,
        String visibility,
        String ownerId,
        Set<String> usersAllowedToRead,
        Set<String> usersAllowedToWrite,
        Instant creationDate,
        Instant lastUpdateDate) {

    public WorldDetails {
        usersAllowedToWrite = Set.copyOf(usersAllowedToWrite);
        usersAllowedToRead = Set.copyOf(usersAllowedToRead);
    }
}
