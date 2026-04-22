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
import me.moirai.storyengine.core.port.outbound.notification.NotificationReader;

public class NotificationReaderImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private NotificationReader reader;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void getNotificationByPublicId_whenNotFound_thenReturnEmpty() {

        // given
        var publicId = UUID.randomUUID();

        // when
        var result = reader.getNotificationByPublicId(publicId, 1L, false);

        // then
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    public void getNotificationByPublicId_whenAdminAndNotificationExists_thenReturn() {

        // given
        var notification = insert(NotificationFixture.broadcast().build(), Notification.class);

        // when
        var result = reader.getNotificationByPublicId(notification.getPublicId(), 1L, true);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get().publicId()).isEqualTo(notification.getPublicId());
    }

    @Test
    public void getNotificationByPublicId_whenUserAndNotificationIsGlobal_thenReturn() {

        // given
        var notification = insert(NotificationFixture.broadcast().build(), Notification.class);

        // when
        var result = reader.getNotificationByPublicId(notification.getPublicId(), 1L, false);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get().publicId()).isEqualTo(notification.getPublicId());
    }

    @Test
    public void getNotificationByPublicId_whenUserAndNotificationIsTargetedToThem_thenReturn() {

        // given
        var userId = 1111L;
        var system = NotificationFixture.system().build();
        ReflectionTestUtils.setField(system, "targetUserId", userId);
        var notification = insert(system, Notification.class);

        // when
        var result = reader.getNotificationByPublicId(notification.getPublicId(), userId, false);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get().publicId()).isEqualTo(notification.getPublicId());
    }

    @Test
    public void getNotificationByPublicId_whenUserAndNotificationIsTargetedToOtherUser_thenReturnEmpty() {

        // given
        var targetUserId = 1111L;
        var otherUserId = 9999L;
        var system = NotificationFixture.system().build();
        ReflectionTestUtils.setField(system, "targetUserId", targetUserId);
        var notification = insert(system, Notification.class);

        // when
        var result = reader.getNotificationByPublicId(notification.getPublicId(), otherUserId, false);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void getNotificationByPublicId_whenUserAndNotificationIsGame_thenReturnEmpty() {

        // given
        var notification = insert(NotificationFixture.game().build(), Notification.class);

        // when
        var result = reader.getNotificationByPublicId(notification.getPublicId(), 1L, false);

        // then
        assertThat(result).isEmpty();
    }
}
