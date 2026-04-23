package me.moirai.storyengine.core.port.inbound.notification;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import me.moirai.storyengine.common.enums.NotificationStatus;
import me.moirai.storyengine.core.domain.notification.NotificationLevel;
import me.moirai.storyengine.core.domain.notification.NotificationType;

public record NotificationDetails(
        UUID publicId,
        String message,
        NotificationType type,
        NotificationLevel level,
        NotificationStatus status,
        String targetUsername,
        UUID adventureId,
        boolean isInteractable,
        Map<String, Object> metadata,
        Instant creationDate,
        Instant lastUpdateDate) {
}
