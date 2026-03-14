package me.moirai.storyengine.core.application.usecase.notification;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;
import java.util.stream.Stream;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.notification.GetNotificationsByUserId;
import me.moirai.storyengine.core.port.inbound.notification.NotificationReadResult;
import me.moirai.storyengine.core.port.inbound.notification.NotificationResult;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;
import me.moirai.storyengine.core.domain.notification.Notification;

@UseCaseHandler
public class GetNotificationsByUserIdHandler
        extends AbstractUseCaseHandler<GetNotificationsByUserId, List<NotificationResult>> {

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
    public List<NotificationResult> execute(GetNotificationsByUserId request) {

        return Stream.concat(repository.findReadByUserId(request.getUserId()).stream(),
                repository.findUnreadByUserId(request.getUserId()).stream())
                .map(notification -> toResult(notification, request.getUserId()))
                .toList();
    }

    private NotificationResult toResult(Notification notification, String userId) {

        return NotificationResult.builder()
                .message(notification.getMessage())
                .senderDiscordId(notification.getSenderDiscordId())
                .receiverDiscordId(notification.getReceiverDiscordId())
                .isGlobal(notification.isGlobal())
                .isInteractable(notification.isInteractable())
                .type(notification.getType().name())
                .notificationsRead(notification.getNotificationsRead().stream()
                        .map(notificationRead -> NotificationReadResult.builder()
                                .userId(notificationRead.getUserId())
                                .readAt(notificationRead.getReadAt())
                                .build())
                        .toList())
                .build();
    }
}
