package me.moirai.storyengine.core.port.inbound.world;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import me.moirai.storyengine.common.domain.Permission;

public record WorldDetails(
        UUID id,
        String name,
        String description,
        String adventureStart,
        String visibility,
        Set<Permission> permissions,
        Instant creationDate,
        Instant lastUpdateDate) {

    public WorldDetails {
        permissions = Set.copyOf(permissions);
    }
}
