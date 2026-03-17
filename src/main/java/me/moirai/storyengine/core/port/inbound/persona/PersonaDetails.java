package me.moirai.storyengine.core.port.inbound.persona;

import java.time.OffsetDateTime;
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
        OffsetDateTime creationDate,
        OffsetDateTime lastUpdateDate) {
}
