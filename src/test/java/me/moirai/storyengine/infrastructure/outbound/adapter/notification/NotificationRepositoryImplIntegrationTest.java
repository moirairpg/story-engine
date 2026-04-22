package me.moirai.storyengine.infrastructure.outbound.adapter.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationFixture;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;

public class NotificationRepositoryImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private NotificationRepository repository;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void deleteAllGameNotificationsByAdventureId_whenGameNotificationsExist_thenDeleteThem() {

        // given
        var adventureId = 4242L;
        var game = NotificationFixture.game().build();
        ReflectionTestUtils.setField(game, "adventureId", adventureId);
        var inserted = insert(game, Notification.class);

        // when
        repository.deleteAllGameNotificationsByAdventureId(adventureId);

        // then
        assertThat(repository.findByPublicId(inserted.getPublicId())).isEmpty();
    }

    @Test
    public void deleteAllGameNotificationsByAdventureId_whenNoGameNotifications_thenDoNothing() {

        // given
        var adventureId = 9999L;

        // when
        repository.deleteAllGameNotificationsByAdventureId(adventureId);

        // then
        assertThat(repository.findByPublicId(UUID.randomUUID())).isEmpty();
    }
}
