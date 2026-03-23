package me.moirai.storyengine.core.port.inbound.persona;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import me.moirai.storyengine.common.enums.Visibility;

public record PersonaDetails(
        UUID id,
        String name,
        String personality,
        Visibility visibility,
        String ownerId,
        Set<String> usersAllowedToWrite,
        Set<String> usersAllowedToRead,
        Instant creationDate,
        Instant lastUpdateDate) {

    public PersonaDetails {
        usersAllowedToWrite = Set.copyOf(usersAllowedToWrite);
        usersAllowedToRead = Set.copyOf(usersAllowedToRead);
    }
}
