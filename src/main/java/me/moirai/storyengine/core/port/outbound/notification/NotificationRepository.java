package me.moirai.storyengine.core.port.outbound.notification;

import java.util.Optional;
import java.util.UUID;

import me.moirai.storyengine.core.domain.notification.Notification;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findByPublicId(UUID publicId);

    void deleteByPublicId(UUID publicId);

    void deleteAllGameNotificationsByAdventureId(Long adventureId);
}
