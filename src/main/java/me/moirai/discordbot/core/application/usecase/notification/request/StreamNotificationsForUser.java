package me.moirai.discordbot.core.application.usecase.notification.request;

import me.moirai.discordbot.common.usecases.UseCase;
import me.moirai.discordbot.core.application.usecase.notification.result.NotificationResult;
import reactor.core.publisher.Flux;

public final class StreamNotificationsForUser extends UseCase<Flux<NotificationResult>>  {

    private final String userId;

    private StreamNotificationsForUser(String userId) {
        this.userId = userId;
    }

    public static StreamNotificationsForUser create(String userId) {
        return new StreamNotificationsForUser(userId);
    }

    public String getUserId() {
        return userId;
    }
}
