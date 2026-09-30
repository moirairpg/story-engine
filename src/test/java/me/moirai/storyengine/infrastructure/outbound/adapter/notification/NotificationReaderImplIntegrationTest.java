package me.moirai.storyengine.infrastructure.outbound.adapter.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import me.moirai.storyengine.AbstractDatabaseIntegrationTest;
import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationFixture;
import me.moirai.storyengine.common.enums.NotificationLevel;
import me.moirai.storyengine.common.enums.NotificationType;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.outbound.notification.NotificationReader;

public class NotificationReaderImplIntegrationTest extends AbstractDatabaseIntegrationTest {

    @Autowired
    private NotificationReader reader;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void getNotificationByPublicId_whenNotFound_thenReturnEmpty() {

        // given
        var publicId = UUID.randomUUID();
        insert(UserFixture.admin().build(), User.class);

        // when
        var result = reader.getNotificationByPublicIdAndRequester(publicId, "john.doe", Role.ADMIN);

        // then
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    public void shouldReturnEmptyWhenNoNotificationMatchesThePublicId() {

        // given
        var publicId = UUID.randomUUID();

        // when
        var result = reader.getNotificationByPublicId(publicId);

        // then
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    public void shouldReturnRecipientIdsAndUsernamesWhenNotificationHasRecipients() {

        // given
        var alice = insert(userWith("alice"), User.class);
        var bob = insert(userWith("bob"), User.class);

        var notification = insert(systemWith(alice.getId(), bob.getId()), Notification.class);
        addRecipient(notification.getId(), alice.getId());
        addRecipient(notification.getId(), bob.getId());

        // when
        var result = reader.getNotificationByPublicId(notification.getPublicId());

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get().recipientUserIds()).containsExactlyInAnyOrder(alice.getId(), bob.getId());
        assertThat(result.get().recipientUsernames()).containsExactlyInAnyOrder("alice", "bob");
    }

    @Test
    public void shouldReturnFewerUsernamesThanRecipientIdsWhenARecipientUserIsMissing() {

        // given
        var alice = insert(userWith("alice"), User.class);

        var notification = insert(systemWith(alice.getId()), Notification.class);
        addRecipient(notification.getId(), alice.getId());
        addRecipient(notification.getId(), 999999L);

        // when
        var result = reader.getNotificationByPublicId(notification.getPublicId());

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get().recipientUserIds()).hasSize(2);
        assertThat(result.get().recipientUsernames()).containsExactly("alice");
    }

    @Test
    public void shouldReturnGameNotificationRegardlessOfRequesterWhenReadByPublicId() {

        // given
        var notification = insert(NotificationFixture.game().build(), Notification.class);

        // when
        var result = reader.getNotificationByPublicId(notification.getPublicId());

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get().type()).isEqualTo(NotificationType.GAME);
    }

    @Test
    public void shouldReturnNotificationWithAllRecipientUsernamesWhenRequesterIsAdmin() {

        // given
        var admin = insert(UserFixture.admin().build(), User.class);
        var alice = insert(userWith("alice"), User.class);
        var bob = insert(userWith("bob"), User.class);

        var system = systemWith(alice.getId(), bob.getId());
        var notification = insert(system, Notification.class);
        addRecipient(notification.getId(), alice.getId());
        addRecipient(notification.getId(), bob.getId());

        // when
        var result = reader.getNotificationByPublicIdAndRequester(notification.getPublicId(),admin.getUsername(), Role.ADMIN);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get().targetUsernames()).containsExactlyInAnyOrder("alice", "bob");
    }

    @Test
    public void shouldReturnNotificationWithOnlyRequesterUsernameWhenRequesterIsNonAdminRecipient() {

        // given
        var alice = insert(userWith("alice"), User.class);
        var bob = insert(userWith("bob"), User.class);

        var system = systemWith(alice.getId(), bob.getId());
        var notification = insert(system, Notification.class);
        addRecipient(notification.getId(), alice.getId());
        addRecipient(notification.getId(), bob.getId());

        // when
        var result = reader.getNotificationByPublicIdAndRequester(notification.getPublicId(),"alice", Role.PLAYER);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get().targetUsernames()).containsExactly("alice");
    }

    @Test
    public void shouldReturnEmptyWhenRequesterIsNotRecipientAndNotAdmin() {

        // given
        var alice = insert(userWith("alice"), User.class);
        insert(userWith("bob"), User.class);

        var system = systemWith(alice.getId());
        var notification = insert(system, Notification.class);
        addRecipient(notification.getId(), alice.getId());

        // when
        var result = reader.getNotificationByPublicIdAndRequester(notification.getPublicId(),"bob", Role.PLAYER);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnBroadcastWithEmptyTargetUsernamesForAnyRequester() {

        // given
        insert(userWith("alice"), User.class);

        var notification = insert(NotificationFixture.broadcast().build(), Notification.class);

        // when
        var result = reader.getNotificationByPublicIdAndRequester(notification.getPublicId(),"alice", Role.PLAYER);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get().targetUsernames()).isEmpty();
    }

    @Test
    public void shouldExcludeGameNotificationFromNonAdminRequester() {

        // given
        insert(userWith("alice"), User.class);

        var notification = insert(NotificationFixture.game().build(), Notification.class);

        // when
        var result = reader.getNotificationByPublicIdAndRequester(notification.getPublicId(),"alice", Role.PLAYER);

        // then
        assertThat(result).isEmpty();
    }

    private User userWith(String username) {

        return User.builder()
                .discordId("discord-" + username)
                .username(username)
                .displayName(username)
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
