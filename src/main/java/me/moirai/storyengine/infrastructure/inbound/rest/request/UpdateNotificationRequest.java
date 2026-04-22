package me.moirai.storyengine.infrastructure.inbound.rest.request;

import me.moirai.storyengine.core.domain.notification.NotificationLevel;

public record UpdateNotificationRequest(
        String message,
        NotificationLevel level) {
}
