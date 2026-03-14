package me.moirai.storyengine.core.domain.notification;

import me.moirai.storyengine.core.port.inbound.notification.SendNotification;
import reactor.core.publisher.Flux;

public interface NotificationService {

    Notification sendNotification(SendNotification createNotification);

    Flux<Notification> streamNotificationsForUser(String userId);
}
