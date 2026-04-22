package me.moirai.storyengine.core.port.outbound.notification;

import java.util.List;

import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;

public interface ActiveSystemNotificationReader {

    List<NotificationDetails> getActiveUnreadSystemNotifications(Long userId);
}
