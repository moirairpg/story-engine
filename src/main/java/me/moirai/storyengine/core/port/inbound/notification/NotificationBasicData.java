package me.moirai.storyengine.core.port.inbound.notification;

import me.moirai.storyengine.core.domain.notification.NotificationType;

public record NotificationBasicData(
        String targetUsername,
        NotificationType type) {
}
