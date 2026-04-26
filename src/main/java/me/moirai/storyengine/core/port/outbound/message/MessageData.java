package me.moirai.storyengine.core.port.outbound.message;

import java.time.Instant;
import java.util.UUID;

import me.moirai.storyengine.common.enums.MessageAuthorRole;
import me.moirai.storyengine.core.domain.message.MessageStatus;

public record MessageData(
        UUID publicId,
        Long adventureId,
        String createdBy,
        MessageAuthorRole role,
        String content,
        Instant creationDate,
        MessageStatus status) {
}
