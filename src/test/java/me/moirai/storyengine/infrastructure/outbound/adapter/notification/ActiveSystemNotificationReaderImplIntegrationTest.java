package me.moirai.storyengine.infrastructure.outbound.adapter.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationFixture;
import me.moirai.storyengine.core.domain.notification.NotificationRead;
import me.moirai.storyengine.core.port.outbound.notification.ActiveSystemNotificationReader;

public class ActiveSystemNotificationReaderImplIntegrationTest extends AbstractIntegrationTest {

    private static final String USERNAME = "some_user";
    private static final Long USER_ID = 1111L;

    @Autowired
    private ActiveSystemNotificationReader reader;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void getActiveUnreadSystemNotifications_whenUnreadNotificationsExist_thenReturnThem() {

        // given
        var system = NotificationFixture.system().build();
        ReflectionTestUtils.setField(system, "targetUserId", USER_ID);
        insert(system, Notification.class);

        // when
        var result = reader.getActiveUnreadSystemNotifications(USERNAME);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    public void getActiveUnreadSystemNotifications_whenAlreadyRead_thenExclude() {

        // given
        var system = NotificationFixture.system().build();
        ReflectionTestUtils.setField(system, "targetUserId", USER_ID);
        var inserted = insert(system, Notification.class);

        var read = NotificationRead.builder()
                .userId(USER_ID)
                .readDate(Instant.now())
                .build();
        ReflectionTestUtils.setField(read, "notificationId", inserted.getId());
        insert(read, NotificationRead.class);

        // when
        var result = reader.getActiveUnreadSystemNotifications(USERNAME);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void getActiveUnreadSystemNotifications_whenNoNotificationsForUser_thenReturnEmpty() {

        // given
        var system = NotificationFixture.system().build();
        ReflectionTestUtils.setField(system, "targetUserId", 9999L);
        insert(system, Notification.class);

        // when
        var result = reader.getActiveUnreadSystemNotifications(USERNAME);

        // then
        assertThat(result).isEmpty();
    }
}
