package me.moirai.storyengine.core.port.inbound.userdetails;

import java.time.Instant;
import java.util.UUID;

import me.moirai.storyengine.common.enums.Role;

public record UserData(
        UUID publicId,
        Long id,
        String discordId,
        String username,
        Role role,
        boolean isActive,
        String bio,
        Instant creationDate) {
}
