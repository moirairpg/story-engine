package me.moirai.storyengine.infrastructure.outbound.adapter.notification;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationFixture;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.port.outbound.notification.ActiveBroadcastNotificationReader;

public class ActiveBroadcastNotificationReaderImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ActiveBroadcastNotificationReader reader;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void getActiveBroadcasts_whenBroadcastsExist_thenReturnThem() {

        // given
        insert(userWith("some_user"), User.class);
        insert(NotificationFixture.broadcast().build(), Notification.class);
        insert(NotificationFixture.urgentBroadcast().build(), Notification.class);

        // when
        var result = reader.getActiveBroadcasts("some_user");

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    public void getActiveBroadcasts_whenNoBroadcasts_thenReturnEmpty() {

        // given
        insert(userWith("some_user"), User.class);
        insert(NotificationFixture.game().build(), Notification.class);

        // when
        var result = reader.getActiveBroadcasts("some_user");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void getActiveBroadcasts_whenRequesterHasDismissedBroadcast_thenExcludeIt() {

        // given
        var someUser = insert(userWith("some_user"), User.class);
        var dismissed = insert(NotificationFixture.broadcast().build(), Notification.class);
        var active = insert(NotificationFixture.urgentBroadcast().build(), Notification.class);
        markRead(dismissed.getId(), someUser.getId());

        // when
        var result = reader.getActiveBroadcasts("some_user");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).publicId()).isEqualTo(active.getPublicId());
    }

    @Test
    public void getActiveBroadcasts_whenDifferentUserDismissedBroadcast_thenStillReturnIt() {

        // given
        insert(userWith("some_user"), User.class);
        var otherUser = insert(userWith("other_user"), User.class);
        var broadcast = insert(NotificationFixture.broadcast().build(), Notification.class);
        markRead(broadcast.getId(), otherUser.getId());

        // when
        var result = reader.getActiveBroadcasts("some_user");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).publicId()).isEqualTo(broadcast.getPublicId());
    }

    private User userWith(String username) {

        return User.builder()
                .discordId("discord-" + username)
                .username(username)
                .role(Role.PLAYER)
                .build();
    }

    private void markRead(Long notificationId, Long userId) {

        jdbcClient.sql("INSERT INTO notification_read (notification_id, user_id, read_date) VALUES (:nid, :uid, NOW())")
                .param("nid", notificationId)
                .param("uid", userId)
                .update();
    }
}
