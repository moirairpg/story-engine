package me.moirai.storyengine.infrastructure.event.notification;

import java.util.Collections;
import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.util.Functions;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.application.event.notification.NotificationCreated;
import me.moirai.storyengine.core.domain.notification.Notification;
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
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
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
                mapToDetails(notification, adventure, Collections.emptyList()));
    }

    private void sendSystemNotification(Notification notification) {

        var userIds = notification.getRecipientUserIds();
        var users = userRepository.findAllById(userIds);

        if (users.size() != userIds.size()) {
            throw new NotFoundException(TARGET_USER_NOT_FOUND);
        }

        for (var user : users) {
            var details = mapToDetails(notification, null, List.of(user.getUsername()));

            messagingTemplate.convertAndSendToUser(
                    user.getUsername(),
                    "/queue/notifications/system",
                    details);
        }
    }

    private void sendBroadcastNotification(Notification notification) {
        messagingTemplate.convertAndSend(
                "/topic/notifications/broadcast",
                mapToDetails(notification, null, Collections.emptyList()));
    }

    private NotificationDetails mapToDetails(
            Notification notification,
            Adventure adventure,
            List<String> targetUsernames) {

        return new NotificationDetails(
                notification.getPublicId(),
                notification.getMessage(),
                notification.getType(),
                notification.getLevel(),
                targetUsernames,
                Functions.mapOrNull(adventure, Adventure::getPublicId),
                notification.isInteractable(),
                notification.getMetadata(),
                notification.getCreationDate(),
                notification.getLastUpdateDate());
    }
}
