package me.moirai.storyengine.core.application.usecase.notification;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;
import java.util.stream.Stream;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.port.inbound.notification.GetNotificationsByUserId;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.inbound.notification.NotificationReadResult;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;

@UseCaseHandler
public class GetNotificationsByUserIdHandler
        extends AbstractUseCaseHandler<GetNotificationsByUserId, List<NotificationDetails>> {

    private final NotificationRepository repository;

    public GetNotificationsByUserIdHandler(NotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(GetNotificationsByUserId request) {

        if (isBlank(request.getUserId())) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }

    @Override
    public List<NotificationDetails> execute(GetNotificationsByUserId request) {

        return Stream.concat(repository.findReadByUserId(request.getUserId()).stream(),
                repository.findUnreadByUserId(request.getUserId()).stream())
                .map(notification -> toResult(notification, request.getUserId()))
                .toList();
    }

    private NotificationDetails toResult(Notification notification, String userId) {

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
