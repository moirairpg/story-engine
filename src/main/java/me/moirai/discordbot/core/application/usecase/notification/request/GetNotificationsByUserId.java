package me.moirai.discordbot.core.application.usecase.notification.request;

import java.util.List;

import me.moirai.discordbot.common.usecases.UseCase;
import me.moirai.discordbot.core.application.usecase.notification.result.NotificationResult;

public class GetNotificationsByUserId extends UseCase<List<NotificationResult>>  {

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
