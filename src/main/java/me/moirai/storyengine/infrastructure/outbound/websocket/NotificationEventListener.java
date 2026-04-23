package me.moirai.storyengine.infrastructure.outbound.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.util.Functions;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationCreated;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@Component
public class NotificationEventListener {

    private static final String NOTIFICATION_NOT_FOUND = "Notification not found after creation event";
    private static final String TARGET_USER_NOT_FOUND = "Target user not found for SYSTEM notification";

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final AdventureRepository adventureRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationEventListener(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            AdventureRepository adventureRepository,
            SimpMessagingTemplate messagingTemplate) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.adventureRepository = adventureRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificationCreated(NotificationCreated event) {

        var notification = notificationRepository.findByPublicId(event.publicId())
                .orElseThrow(() -> new NotFoundException(NOTIFICATION_NOT_FOUND));

        switch (notification.getType()) {
            case BROADCAST -> sendBroadcastNotification(notification);
            case SYSTEM -> sendSystemNotification(notification);
            case GAME -> sendAdventureNotification(notification);
        }
    }

    private void sendAdventureNotification(Notification notification) {
        var adventure = adventureRepository.findById(notification.getAdventureId())
                .orElseThrow(() -> new NotFoundException("Adventure not found"));

        messagingTemplate.convertAndSend(
                "/topic/notifications/adventure/" + adventure.getPublicId(),
                mapToDetails(notification, adventure, null));
    }

    private void sendSystemNotification(Notification notification) {
        var targetUser = userRepository.findById(notification.getTargetUserId())
                .orElseThrow(() -> new NotFoundException(TARGET_USER_NOT_FOUND));

        messagingTemplate.convertAndSendToUser(
                targetUser.getUsername(),
                "/queue/notifications/system",
                mapToDetails(notification, null, targetUser));
    }

    private void sendBroadcastNotification(Notification notification) {
        messagingTemplate.convertAndSend(
                "/topic/notifications/broadcast",
                mapToDetails(notification, null, null));
    }

    private NotificationDetails mapToDetails(
            Notification notification,
            Adventure adventure,
            User user) {

        return new NotificationDetails(
                notification.getPublicId(),
                notification.getMessage(),
                notification.getType(),
                notification.getLevel(),
                Functions.mapOrNull(user, u -> notification.getStatus(u.getId())),
                Functions.mapOrNull(user, User::getUsername),
                Functions.mapOrNull(adventure, Adventure::getPublicId),
                notification.isInteractable(),
                notification.getMetadata(),
                notification.getCreationDate(),
                notification.getLastUpdateDate());
    }
}
