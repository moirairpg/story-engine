package me.moirai.storyengine.core.application.usecase.notification;

import static org.apache.commons.lang3.StringUtils.isBlank;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationType;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.inbound.notification.SendNotification;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;
import reactor.core.publisher.Sinks;

@UseCaseHandler
public class SendNotificationHandler extends AbstractUseCaseHandler<SendNotification, NotificationDetails> {

    private final NotificationRepository notificationRepository;
    private final Sinks.Many<String> sink;

    public SendNotificationHandler(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
    }

    @Override
    public void validate(SendNotification request) {

        if (isBlank(request.getMessage())) {
            throw new IllegalArgumentException("Notification message cannot be null");
        }

        if (isBlank(request.getType())) {
            throw new IllegalArgumentException("Notification type cannot be null");
        }

        if (isBlank(request.getSenderDiscordId())) {
            throw new IllegalArgumentException("Notification sender cannot be null");
        }

        if (isBlank(request.getReceiverDiscordId()) && !request.isGlobal()) {
            throw new IllegalArgumentException("The receiver ID cannot be null when a notification is not global");
        }
    }

    @Override
    public NotificationDetails execute(SendNotification request) {

        Notification notification = notificationRepository.save(Notification.builder()
                .isGlobal(request.isGlobal())
                .isInteractable(request.isInteractable())
                .message(request.getMessage())
                .metadata(request.getMetadata())
                .receiverDiscordId(request.getReceiverDiscordId())
                .senderDiscordId(request.getSenderDiscordId())
                .type(NotificationType.fromString(request.getType()))
                .build());

        sink.tryEmitNext(notification.getId());
        return mapResult(notification);
    }

    private NotificationDetails mapResult(Notification notification) {

        return NotificationDetails.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .senderDiscordId(notification.getSenderDiscordId())
                .receiverDiscordId(notification.getReceiverDiscordId())
                .isGlobal(notification.isGlobal())
                .isInteractable(notification.isInteractable())
                .type(notification.getType().name())
                .metadata(notification.getMetadata())
                .creationDate(notification.getCreationDate())
                .lastUpdateDate(notification.getLastUpdateDate())
                .build();
    }
}
