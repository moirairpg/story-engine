package me.moirai.storyengine.core.port.inbound.userdetails;

import java.time.Instant;
import java.util.UUID;

import me.moirai.storyengine.common.enums.Role;

public record UserDetailsResult(
        UUID publicId,
        Long id,
        String discordUsername,
        String username,
        String displayName,
        String avatarUrl,
        Role role,
        boolean isActive,
        String bio,
        Instant creationDate) {
}
