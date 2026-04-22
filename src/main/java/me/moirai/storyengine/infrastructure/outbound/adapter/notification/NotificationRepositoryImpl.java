package me.moirai.storyengine.infrastructure.outbound.adapter.notification;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;

@Repository
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;

    public NotificationRepositoryImpl(NotificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Notification save(Notification notification) {
        return jpaRepository.save(notification);
    }

    @Override
    public Optional<Notification> findByPublicId(UUID publicId) {
        return jpaRepository.findByPublicId(publicId);
    }

    @Override
    public void deleteByPublicId(UUID publicId) {
        jpaRepository.deleteByPublicId(publicId);
    }

    @Override
    public void deleteAllGameNotificationsByAdventureId(Long adventureId) {
        jpaRepository.deleteAllGameNotificationsByAdventureId(adventureId);
    }
}
