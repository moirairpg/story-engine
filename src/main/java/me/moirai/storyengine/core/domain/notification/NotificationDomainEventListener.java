package me.moirai.storyengine.core.domain.notification;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import me.moirai.storyengine.core.domain.adventure.AdventureDeletedEvent;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;

@Component
public class NotificationDomainEventListener {

    private final NotificationRepository notificationRepository;

    public NotificationDomainEventListener(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onAdventureDeleted(AdventureDeletedEvent event) {

        notificationRepository.deleteAllGameNotificationsByAdventureId(event.getAdventureId());
    }
}
