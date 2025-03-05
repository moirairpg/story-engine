package me.moirai.discordbot.core.application.usecase.notification.request;

import me.moirai.discordbot.common.usecases.UseCase;
import me.moirai.discordbot.core.application.usecase.notification.result.NotificationReadResult;

public final class ReadNotification extends UseCase<NotificationReadResult> {

    private final String userId;
    private final String notificationId;

    private ReadNotification(String userId, String notificationId) {
        this.userId = userId;
        this.notificationId = notificationId;
    }

    public static ReadNotification create(String userId, String notificationId) {
        return new ReadNotification(userId, notificationId);
    }

    public String getUserId() {
        return userId;
    }

    public String getNotificationId() {
        return notificationId;
    }
}
