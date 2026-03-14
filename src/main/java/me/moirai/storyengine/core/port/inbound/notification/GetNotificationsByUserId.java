package me.moirai.storyengine.core.port.inbound.notification;

import java.util.List;

import me.moirai.storyengine.common.usecases.UseCase;

public class GetNotificationsByUserId extends UseCase<List<NotificationDetails>>  {

    private final String userId;

    private GetNotificationsByUserId(String userId) {
        this.userId = userId;
    }

    public static GetNotificationsByUserId create(String userId) {
        return new GetNotificationsByUserId(userId);
    }

    public String getUserId() {
        return userId;
    }
}
