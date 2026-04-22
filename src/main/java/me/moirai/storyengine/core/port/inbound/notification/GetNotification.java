package me.moirai.storyengine.core.port.inbound.notification;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.query.Query;

public record GetNotification(
        UUID notificationId,
        Long requesterId,
        boolean isAdmin)
        implements Query<NotificationDetails> {
}
