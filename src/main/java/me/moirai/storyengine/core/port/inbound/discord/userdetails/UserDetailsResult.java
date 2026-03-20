package me.moirai.storyengine.core.port.inbound.discord.userdetails;

import java.time.OffsetDateTime;
import java.util.UUID;

import me.moirai.storyengine.common.enums.Role;

public record UserDetailsResult(
        UUID publicId,
        String discordId,
        String username,
        String nickname,
        String avatarUrl,
        Role role,
        OffsetDateTime creationDate) {
}
