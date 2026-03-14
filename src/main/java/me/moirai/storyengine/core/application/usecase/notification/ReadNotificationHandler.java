package me.moirai.storyengine.core.application.usecase.notification;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.time.OffsetDateTime;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.notification.ReadNotification;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;
import me.moirai.storyengine.core.port.inbound.notification.NotificationReadResult;
import me.moirai.storyengine.core.domain.notification.Notification;

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
