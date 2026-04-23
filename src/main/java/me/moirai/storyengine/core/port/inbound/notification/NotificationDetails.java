package me.moirai.storyengine.core.port.inbound.notification;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.moirai.storyengine.common.util.Functions;
import me.moirai.storyengine.core.domain.notification.NotificationLevel;
import me.moirai.storyengine.core.domain.notification.NotificationType;

public record NotificationDetails(
        UUID publicId,
        String message,
        NotificationType type,
        NotificationLevel level,
        List<String> targetUsernames,
        UUID adventureId,
        boolean isInteractable,
        Map<String, Object> metadata,
        Instant creationDate,
        Instant lastUpdateDate) {

    public NotificationDetails {
        targetUsernames = Functions.mapOrDefault(targetUsernames, List.of(), Collections::unmodifiableList);
    }
}
