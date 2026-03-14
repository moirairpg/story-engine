package me.moirai.storyengine.core.port.inbound.notification;

import me.moirai.storyengine.common.usecases.UseCase;
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
