package me.moirai.storyengine.infrastructure.outbound.adapter.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationFixture;
import me.moirai.storyengine.core.domain.notification.NotificationRead;
import me.moirai.storyengine.core.port.outbound.notification.ActiveBroadcastNotificationReader;

public class ActiveBroadcastNotificationReaderImplIntegrationTest extends AbstractIntegrationTest {

    private static final String USERNAME = "some_user";

    @Autowired
    private ActiveBroadcastNotificationReader reader;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void getActiveBroadcasts_whenBroadcastsExist_thenReturnThem() {

        // given
        insert(NotificationFixture.broadcast().build(), Notification.class);
        insert(NotificationFixture.urgentBroadcast().build(), Notification.class);

        // when
        var result = reader.getActiveBroadcasts(USERNAME);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    public void getActiveBroadcasts_whenNoBroadcasts_thenReturnEmpty() {

        // given
        insert(NotificationFixture.game().build(), Notification.class);

        // when
        var result = reader.getActiveBroadcasts(USERNAME);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void getActiveBroadcasts_whenRequesterHasDismissedBroadcast_thenExcludeIt() {

        // given
        var dismissed = insert(NotificationFixture.broadcast().build(), Notification.class);
        var active = insert(NotificationFixture.urgentBroadcast().build(), Notification.class);

        var readRow = NotificationRead.builder()
                .notification(dismissed)
                .userId(1111L)
                .readDate(Instant.now())
                .build();
        insert(readRow, NotificationRead.class);

        // when
        var result = reader.getActiveBroadcasts(USERNAME);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).publicId()).isEqualTo(active.getPublicId());
    }

    @Test
    public void getActiveBroadcasts_whenDifferentUserDismissedBroadcast_thenStillReturnIt() {

        // given
        var broadcast = insert(NotificationFixture.broadcast().build(), Notification.class);

        var readRow = NotificationRead.builder()
                .notification(broadcast)
                .userId(9999L)
                .readDate(Instant.now())
                .build();
        insert(readRow, NotificationRead.class);

        // when
        var result = reader.getActiveBroadcasts(USERNAME);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).publicId()).isEqualTo(broadcast.getPublicId());
    }
}
