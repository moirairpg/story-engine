package me.moirai.storyengine.core.port.inbound.adventure;

import java.time.Instant;
import java.util.UUID;

import me.moirai.storyengine.common.enums.AiRole;
import me.moirai.storyengine.core.domain.message.MessageStatus;

public record MessageSummary(
        UUID id,
        AiRole role,
        String content,
        MessageStatus status,
        String authorUsername,
        Instant creationDate) {
}
