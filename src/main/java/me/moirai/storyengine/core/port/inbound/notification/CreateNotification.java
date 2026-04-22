package me.moirai.storyengine.core.port.inbound.notification;

import java.util.List;
import java.util.Map;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.core.domain.notification.NotificationLevel;
import me.moirai.storyengine.core.domain.notification.NotificationType;

public record CreateNotification(
        String message,
        NotificationType type,
        NotificationLevel level,
        List<String> targetUsernames,
        Long adventureId,
        boolean isInteractable,
        Map<String, Object> metadata)
        implements Command<List<NotificationDetails>> {
}
