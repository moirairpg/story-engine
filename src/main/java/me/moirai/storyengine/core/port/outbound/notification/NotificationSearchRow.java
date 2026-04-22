package me.moirai.storyengine.core.port.outbound.notification;

import java.time.Instant;
import java.util.UUID;

import me.moirai.storyengine.common.enums.NotificationStatus;
import me.moirai.storyengine.core.domain.notification.NotificationLevel;
import me.moirai.storyengine.core.domain.notification.NotificationType;

public record NotificationSearchRow(
        UUID publicId,
        String message,
        NotificationType type,
        NotificationLevel level,
        NotificationStatus status,
        Instant creationDate) {
}
