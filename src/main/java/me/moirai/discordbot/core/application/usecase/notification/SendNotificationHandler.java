package me.moirai.discordbot.core.application.usecase.notification;

import static org.apache.commons.lang3.StringUtils.isBlank;

import me.moirai.discordbot.common.annotation.UseCaseHandler;
import me.moirai.discordbot.common.usecases.AbstractUseCaseHandler;
import me.moirai.discordbot.core.application.usecase.notification.request.SendNotification;
import me.moirai.discordbot.core.application.usecase.notification.result.SendNotificationResult;
import me.moirai.discordbot.core.domain.notification.Notification;
import me.moirai.discordbot.core.domain.notification.NotificationService;

@UseCaseHandler
public class SendNotificationHandler extends AbstractUseCaseHandler<SendNotification, SendNotificationResult> {

    private final NotificationService notificationService;

    public SendNotificationHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
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
    public SendNotificationResult execute(SendNotification request) {

        Notification notification = notificationService.sendNotification(request);
        return SendNotificationResult.withIdAndCreationDateTime(notification.getId(), notification.getCreationDate());
    }
}
