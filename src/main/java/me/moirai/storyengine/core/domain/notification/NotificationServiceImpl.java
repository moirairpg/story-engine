package me.moirai.storyengine.core.domain.notification;

import static io.micrometer.common.util.StringUtils.isNotBlank;

import me.moirai.storyengine.common.annotation.DomainService;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.port.inbound.notification.SendNotification;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@DomainService
public class NotificationServiceImpl implements NotificationService {

    private static final String NOTIFICATION_NOT_FOUND = "Notification to be retrieved was not found";

    private final NotificationRepository notificationRepository;
    private final Sinks.Many<String> sink;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
    }

    @Override
    public Notification sendNotification(SendNotification createNotification) {

        Notification notification = notificationRepository.save(Notification.builder()
                .isGlobal(createNotification.isGlobal())
                .isInteractable(createNotification.isInteractable())
                .message(createNotification.getMessage())
                .metadata(createNotification.getMetadata())
                .receiverDiscordId(createNotification.getReceiverDiscordId())
                .senderDiscordId(createNotification.getSenderDiscordId())
                .type(NotificationType.fromString(createNotification.getType()))
                .build());

        sink.tryEmitNext(notification.getId());

        return notification;
    }

    @Override
    public Flux<Notification> streamNotificationsForUser(String userId) {

        return sink.asFlux()
                .map(notificationId -> notificationRepository.findById(notificationId)
                        .orElseThrow(() -> new AssetNotFoundException(NOTIFICATION_NOT_FOUND)))
                .filter(notification -> belongsToUser(userId, notification))
                .filter(notification -> !notification.isReadByUserId(userId));
    }

    private boolean belongsToUser(String userId, Notification notification) {

        return notification.isGlobal() || (isNotBlank(notification.getReceiverDiscordId())
                && notification.getReceiverDiscordId().equals(userId));
    }
}
