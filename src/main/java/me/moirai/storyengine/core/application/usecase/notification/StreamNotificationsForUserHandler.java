package me.moirai.storyengine.core.application.usecase.notification;

import static org.apache.commons.lang3.StringUtils.isBlank;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.application.usecase.notification.request.StreamNotificationsForUser;
import me.moirai.storyengine.core.application.usecase.notification.result.NotificationResult;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationService;
import reactor.core.publisher.Flux;

@UseCaseHandler
public class StreamNotificationsForUserHandler
        extends AbstractUseCaseHandler<StreamNotificationsForUser, Flux<NotificationResult>> {

    private final NotificationService notificationService;

    public StreamNotificationsForUserHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void validate(StreamNotificationsForUser request) {

        if (isBlank(request.getUserId())) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }

    @Override
    public Flux<NotificationResult> execute(StreamNotificationsForUser request) {

        return notificationService.streamNotificationsForUser(request.getUserId())
                .map(this::toResult);
    }

    private NotificationResult toResult(Notification notification) {

        return NotificationResult.builder()
                .isGlobal(notification.isGlobal())
                .isInteractable(notification.isInteractable())
                .message(notification.getMessage())
                .metadata(notification.getMetadata())
                .receiverDiscordId(notification.getReceiverDiscordId())
                .senderDiscordId(notification.getSenderDiscordId())
                .type(notification.getType().name())
                .build();
    }
}
