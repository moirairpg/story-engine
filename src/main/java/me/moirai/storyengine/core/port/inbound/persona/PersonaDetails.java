package me.moirai.storyengine.core.port.inbound.persona;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.enums.Visibility;

public record PersonaDetails(
        UUID id,
        String name,
        String personality,
        Visibility visibility,
        Set<PermissionDto> permissions,
        Instant creationDate,
        Instant lastUpdateDate) {

    public PersonaDetails {
        permissions = Set.copyOf(permissions);
    }
}
