package me.moirai.storyengine.core.port.inbound.message;

import java.time.Instant;
import java.util.UUID;

import me.moirai.storyengine.common.enums.MessageAuthorRole;

public record MessageResult(
        UUID id,
        String content,
        MessageAuthorRole role,
        Instant createdAt) {
}
