package me.moirai.storyengine.infrastructure.outbound.adapter.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationFixture;
import me.moirai.storyengine.core.domain.notification.NotificationLevel;
import me.moirai.storyengine.core.domain.notification.NotificationType;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.port.outbound.notification.NotificationBasicDataReader;

public class NotificationBasicDataReaderImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private NotificationBasicDataReader reader;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void shouldReturnRecipientUsernamesForSystemNotification() {

        // given
        var alice = insert(userWith("alice"), User.class);
        var bob = insert(userWith("bob"), User.class);

        var notification = insert(systemWith(alice.getId(), bob.getId()), Notification.class);
        addRecipient(notification.getId(), alice.getId());
        addRecipient(notification.getId(), bob.getId());

        // when
        var result = reader.getByPublicId(notification.getPublicId());

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get().type()).isEqualTo(NotificationType.SYSTEM);
        assertThat(result.get().targetUsernames()).containsExactlyInAnyOrder("alice", "bob");
    }

    @Test
    public void shouldReturnEmptyRecipientsForBroadcast() {

        // given
        var notification = insert(NotificationFixture.broadcast().build(), Notification.class);

        // when
        var result = reader.getByPublicId(notification.getPublicId());

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get().type()).isEqualTo(NotificationType.BROADCAST);
        assertThat(result.get().targetUsernames()).isEmpty();
    }

    @Test
    public void shouldReturnEmptyRecipientsForGame() {

        // given
        var notification = insert(NotificationFixture.game().build(), Notification.class);

        // when
        var result = reader.getByPublicId(notification.getPublicId());

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get().type()).isEqualTo(NotificationType.GAME);
        assertThat(result.get().targetUsernames()).isEmpty();
    }

    @Test
    public void shouldReturnEmptyOptionalWhenNotificationNotFound() {

        // given
        var publicId = UUID.randomUUID();

        // when
        var result = reader.getByPublicId(publicId);

        // then
        assertThat(result).isEmpty();
    }

    private User userWith(String username) {

        return User.builder()
                .discordId("discord-" + username)
                .username(username)
                .role(Role.PLAYER)
                .build();
    }

    private Notification systemWith(Long... recipientIds) {

        var builder = Notification.builder()
                .message("System message")
                .type(NotificationType.SYSTEM)
                .level(NotificationLevel.INFO);

        for (var id : recipientIds) {
            builder.recipientUserId(id);
        }

        return builder.build();
    }

    private void addRecipient(Long notificationId, Long userId) {

        jdbcClient.sql("INSERT INTO notification_recipient (notification_id, user_id) VALUES (:nid, :uid)")
                .param("nid", notificationId)
                .param("uid", userId)
                .update();
    }
}
