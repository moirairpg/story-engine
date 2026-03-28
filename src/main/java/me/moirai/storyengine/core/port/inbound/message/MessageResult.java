package me.moirai.storyengine.core.port.inbound.message;

import java.time.Instant;
import java.util.UUID;

import me.moirai.storyengine.common.enums.AiRole;

public record MessageResult(
        UUID id,
        String content,
        AiRole role,
        Instant createdAt) {
}
