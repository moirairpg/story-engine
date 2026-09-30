package me.moirai.storyengine.core.port.outbound.userdetails;

import java.time.Instant;
import java.util.UUID;

import me.moirai.storyengine.common.enums.Role;

public record UserData(
        UUID publicId,
        Long id,
        String discordId,
        String username,
        String displayName,
        Role role,
        boolean isActive,
        String bio,
        Instant creationDate) {
}
