package me.moirai.storyengine.core.application.event;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.notification.NotificationCreated;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@Component
public class NotificationEventListener {

    private static final String NOTIFICATION_NOT_FOUND = "Notification not found after creation event";
    private static final String TARGET_USER_NOT_FOUND = "Target user not found for SYSTEM notification";

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationEventListener(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificationCreated(NotificationCreated event) {

        var notification = notificationRepository.findByPublicId(event.publicId())
                .orElseThrow(() -> new NotFoundException(NOTIFICATION_NOT_FOUND));

        switch (notification.getType()) {
            case BROADCAST -> messagingTemplate.convertAndSend(
                    "/topic/notifications/broadcast", notification);
            case SYSTEM -> {
                var targetUser = userRepository.findById(notification.getTargetUserId())
                        .orElseThrow(() -> new NotFoundException(TARGET_USER_NOT_FOUND));

                messagingTemplate.convertAndSendToUser(
                        targetUser.getUsername(), "/queue/notifications/system", notification);
            }
            case GAME -> messagingTemplate.convertAndSend(
                    "/topic/notifications/game/" + notification.getAdventureId(), notification);
        }
    }
}
