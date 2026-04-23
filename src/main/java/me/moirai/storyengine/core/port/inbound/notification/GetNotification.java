package me.moirai.storyengine.core.port.inbound.notification;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.query.Query;
import me.moirai.storyengine.common.enums.Role;

public record GetNotification(
        UUID notificationId,
        String requesterUsername,
        Role requesterRole)
        implements Query<NotificationDetails> {
}
