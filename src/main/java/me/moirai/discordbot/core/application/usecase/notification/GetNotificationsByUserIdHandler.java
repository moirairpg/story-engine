package me.moirai.discordbot.core.application.usecase.notification;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;
import java.util.stream.Stream;

import me.moirai.discordbot.common.annotation.UseCaseHandler;
import me.moirai.discordbot.common.usecases.AbstractUseCaseHandler;
import me.moirai.discordbot.core.application.usecase.notification.request.GetNotificationsByUserId;
import me.moirai.discordbot.core.application.usecase.notification.result.NotificationReadResult;
import me.moirai.discordbot.core.application.usecase.notification.result.NotificationResult;
import me.moirai.discordbot.core.domain.notification.Notification;
import me.moirai.discordbot.core.domain.notification.NotificationRepository;

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
