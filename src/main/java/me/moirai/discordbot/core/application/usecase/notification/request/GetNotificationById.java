package me.moirai.discordbot.core.application.usecase.notification.request;

import me.moirai.discordbot.common.usecases.UseCase;
import me.moirai.discordbot.core.application.usecase.notification.result.NotificationResult;

public class GetNotificationById extends UseCase<NotificationResult>  {

    private final String notificationId;

    private GetNotificationById(String notificationId) {
        this.notificationId = notificationId;
    }

    public static GetNotificationById create(String notificationId) {
        return new GetNotificationById(notificationId);
    }

    public String getNotificationId() {
        return notificationId;
    }
}
