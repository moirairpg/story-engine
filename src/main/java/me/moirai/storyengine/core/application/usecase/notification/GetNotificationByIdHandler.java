package me.moirai.storyengine.core.application.usecase.notification;

import static org.apache.commons.lang3.StringUtils.isBlank;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.port.inbound.notification.GetNotificationById;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.inbound.notification.NotificationReadResult;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;

@UseCaseHandler
public class GetNotificationByIdHandler extends AbstractUseCaseHandler<GetNotificationById, NotificationDetails> {

    private final NotificationRepository repository;

    public GetNotificationByIdHandler(NotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(GetNotificationById request) {

        if (isBlank(request.getNotificationId())) {
            throw new IllegalArgumentException("Notification ID cannot be null");
        }
    }

    @Override
    public NotificationDetails execute(GetNotificationById request) {

        return repository.findById(request.getNotificationId())
                .map(this::toResult)
                .orElseThrow(() -> new AssetNotFoundException("Notification not found"));
    }

    private NotificationDetails toResult(Notification notification) {

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
                .notificationsRead(notification.getNotificationsRead().stream()
                        .map(notificationRead -> NotificationReadResult.builder()
                                .userId(notificationRead.getUserId())
                                .readAt(notificationRead.getReadAt())
                                .build())
                        .toList())
                .build();
    }
}
