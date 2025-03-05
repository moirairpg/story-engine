package me.moirai.discordbot.core.domain.notification;

import me.moirai.discordbot.core.application.usecase.notification.request.SendNotification;
import reactor.core.publisher.Flux;

public interface NotificationService {

    Notification sendNotification(SendNotification createNotification);

    Flux<Notification> streamNotificationsForUser(String userId);
}
