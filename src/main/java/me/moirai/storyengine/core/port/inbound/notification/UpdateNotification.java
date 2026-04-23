package me.moirai.storyengine.core.port.inbound.notification;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.core.domain.notification.NotificationLevel;

public record UpdateNotification(
        UUID notificationId,
        String message,
        NotificationLevel level)
        implements Command<NotificationDetails> {
}
