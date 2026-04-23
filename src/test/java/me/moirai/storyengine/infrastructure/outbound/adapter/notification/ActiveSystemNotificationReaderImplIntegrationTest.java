package me.moirai.storyengine.infrastructure.outbound.adapter.notification;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationLevel;
import me.moirai.storyengine.core.domain.notification.NotificationType;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.port.outbound.notification.ActiveSystemNotificationReader;

public class ActiveSystemNotificationReaderImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ActiveSystemNotificationReader reader;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void shouldReturnSystemNotificationsWhereUserIsRecipientAndUnread() {

        // given
        var alice = insert(userWith("alice"), User.class);
        var notification = insert(systemWith(alice.getId()), Notification.class);
        addRecipient(notification.getId(), alice.getId());

        // when
        var result = reader.getActiveUnreadSystemNotifications("alice");

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    public void shouldReturnTargetUsernamesContainingOnlyTheRequestingUser() {

        // given
        var alice = insert(userWith("alice"), User.class);
        var bob = insert(userWith("bob"), User.class);
        var notification = insert(systemWith(alice.getId(), bob.getId()), Notification.class);
        addRecipient(notification.getId(), alice.getId());
        addRecipient(notification.getId(), bob.getId());

        // when
        var result = reader.getActiveUnreadSystemNotifications("alice");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).targetUsernames()).containsExactly("alice");
    }

    @Test
    public void shouldExcludeSystemNotificationsAlreadyReadByUser() {

        // given
        var alice = insert(userWith("alice"), User.class);
        var notification = insert(systemWith(alice.getId()), Notification.class);
        addRecipient(notification.getId(), alice.getId());
        markRead(notification.getId(), alice.getId());

        // when
        var result = reader.getActiveUnreadSystemNotifications("alice");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldNotReturnSystemNotificationsForUsersNotInRecipients() {

        // given
        var alice = insert(userWith("alice"), User.class);
        insert(userWith("bob"), User.class);
        var notification = insert(systemWith(alice.getId()), Notification.class);
        addRecipient(notification.getId(), alice.getId());

        // when
        var result = reader.getActiveUnreadSystemNotifications("bob");

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

    private void markRead(Long notificationId, Long userId) {

        jdbcClient.sql("INSERT INTO notification_read (notification_id, user_id, read_date) VALUES (:nid, :uid, NOW())")
                .param("nid", notificationId)
                .param("uid", userId)
                .update();
    }
}
