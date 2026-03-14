package me.moirai.storyengine.core.port.inbound.notification;

import me.moirai.storyengine.common.usecases.UseCase;

public class GetNotificationById extends UseCase<NotificationDetails>  {

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
