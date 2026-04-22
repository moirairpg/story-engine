package me.moirai.storyengine.infrastructure.outbound.adapter.notification;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import me.moirai.storyengine.core.domain.notification.Notification;

public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByPublicId(UUID publicId);

    void deleteByPublicId(UUID publicId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.type = 'GAME' AND n.adventureId = :adventureId")
    void deleteAllGameNotificationsByAdventureId(Long adventureId);
}
