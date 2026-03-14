package me.moirai.storyengine.core.application.usecase.notification;

import static io.micrometer.common.util.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.port.inbound.notification.NotificationResult;
import me.moirai.storyengine.core.port.inbound.notification.StreamNotificationsForUser;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@UseCaseHandler
public class StreamNotificationsForUserHandler
        extends AbstractUseCaseHandler<StreamNotificationsForUser, Flux<NotificationResult>> {

    private static final String NOTIFICATION_NOT_FOUND = "Notification to be retrieved was not found";

    private final NotificationRepository notificationRepository;
    private final Sinks.Many<String> sink;

    public StreamNotificationsForUserHandler(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
    }

    @Override
    public void validate(StreamNotificationsForUser request) {

        if (isBlank(request.getUserId())) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }

    @Override
    public Flux<NotificationResult> execute(StreamNotificationsForUser request) {

        return sink.asFlux()
                .map(notificationId -> notificationRepository.findById(notificationId)
                        .orElseThrow(() -> new AssetNotFoundException(NOTIFICATION_NOT_FOUND)))
                .filter(notification -> belongsToUser(request.getUserId(), notification))
                .filter(notification -> !notification.isReadByUserId(request.getUserId()))
                .map(this::toResult);
    }

    private boolean belongsToUser(String userId, Notification notification) {

        return notification.isGlobal() || (isNotBlank(notification.getReceiverDiscordId())
                && notification.getReceiverDiscordId().equals(userId));
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
