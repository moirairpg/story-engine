package me.moirai.discordbot.core.application.usecase.notification;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.time.OffsetDateTime;

import me.moirai.discordbot.common.annotation.UseCaseHandler;
import me.moirai.discordbot.common.exception.AssetNotFoundException;
import me.moirai.discordbot.common.exception.BusinessRuleViolationException;
import me.moirai.discordbot.common.usecases.AbstractUseCaseHandler;
import me.moirai.discordbot.core.application.usecase.notification.request.ReadNotification;
import me.moirai.discordbot.core.application.usecase.notification.result.NotificationReadResult;
import me.moirai.discordbot.core.domain.notification.Notification;
import me.moirai.discordbot.core.domain.notification.NotificationRepository;

@UseCaseHandler
public class ReadNotificationHandler
        extends AbstractUseCaseHandler<ReadNotification, NotificationReadResult> {

    private final NotificationRepository repository;

    public ReadNotificationHandler(NotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(ReadNotification request) {

        if (isBlank(request.getNotificationId())) {
            throw new IllegalArgumentException("Notification ID cannot be null");
        }

        if (isBlank(request.getUserId())) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }

    @Override
    public NotificationReadResult execute(ReadNotification request) {

        Notification notification = repository.findById(request.getNotificationId())
                .orElseThrow(() -> new AssetNotFoundException("Notification not found"));

        if (notification.isReadByUserId(request.getUserId())) {
            throw new BusinessRuleViolationException("This message has already been read by this user");
        }

        OffsetDateTime readAt = notification.markAsRead(request.getUserId());
        return NotificationReadResult.builder()
                .readAt(readAt)
                .userId(request.getUserId())
                .build();
    }
}
