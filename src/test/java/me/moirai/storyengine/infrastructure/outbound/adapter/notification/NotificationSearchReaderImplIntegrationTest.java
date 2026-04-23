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
import me.moirai.storyengine.core.domain.notification.NotificationLevel;
import me.moirai.storyengine.core.domain.notification.NotificationType;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.port.inbound.notification.SearchNotifications;
import me.moirai.storyengine.core.port.outbound.notification.NotificationSearchReader;

public class NotificationSearchReaderImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private NotificationSearchReader reader;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void search_whenNoFilters_thenReturnAllNotifications() {

        // given
        var alice = insert(userWith("alice"), User.class);
        insert(NotificationFixture.broadcast().build(), Notification.class);
        var system = insert(systemWith(alice.getId()), Notification.class);
        addRecipient(system.getId(), alice.getId());
        insert(NotificationFixture.game().build(), Notification.class);

        var query = new SearchNotifications(null, null, null, null, null, 1, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result.totalItems()).isEqualTo(3);
    }

    @Test
    public void search_whenFilteredByType_thenReturnMatchingOnly() {

        // given
        var alice = insert(userWith("alice"), User.class);
        insert(NotificationFixture.broadcast().build(), Notification.class);
        var system = insert(systemWith(alice.getId()), Notification.class);
        addRecipient(system.getId(), alice.getId());

        var query = new SearchNotifications(NotificationType.BROADCAST, null, null, null,
                null, 1, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result.totalItems()).isEqualTo(1);
        assertThat(result.data().get(0).type()).isEqualTo(NotificationType.BROADCAST);
    }

    @Test
    public void search_whenFilteredByLevel_thenReturnMatchingOnly() {

        // given
        insert(NotificationFixture.broadcast().build(), Notification.class);
        insert(NotificationFixture.urgentBroadcast().build(), Notification.class);

        var query = new SearchNotifications(null, NotificationLevel.URGENT, null, null,
                null, 1, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result.totalItems()).isEqualTo(1);
        assertThat(result.data().get(0).level()).isEqualTo(NotificationLevel.URGENT);
    }

    @Test
    public void shouldFilterByReceiverIdViaRecipientJoin() {

        // given
        var alice = insert(userWith("alice"), User.class);
        var bob = insert(userWith("bob"), User.class);

        var aliceNotification = insert(systemWith(alice.getId()), Notification.class);
        addRecipient(aliceNotification.getId(), alice.getId());

        var bobNotification = insert(systemWith(bob.getId()), Notification.class);
        addRecipient(bobNotification.getId(), bob.getId());

        var query = new SearchNotifications(null, null, alice.getPublicId(), null, null, 1, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result.totalItems()).isEqualTo(1);
        assertThat(result.data().get(0).publicId()).isEqualTo(aliceNotification.getPublicId());
    }

    @Test
    public void shouldReturnSummariesWithoutStatusField() {

        // given
        insert(NotificationFixture.broadcast().build(), Notification.class);

        var query = new SearchNotifications(null, null, null, null, null, 1, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result.data()).hasSize(1);
        var summary = result.data().get(0);
        assertThat(summary.publicId()).isNotNull();
        assertThat(summary.message()).isEqualTo("Broadcast message");
        assertThat(summary.type()).isEqualTo(NotificationType.BROADCAST);
        assertThat(summary.level()).isEqualTo(NotificationLevel.INFO);
        assertThat(summary.creationDate()).isNotNull();
    }

    @Test
    public void search_whenNoMatchingResults_thenReturnEmptyPage() {

        // given
        var query = new SearchNotifications(null, null, null, null, null, 1, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result.totalItems()).isEqualTo(0);
        assertThat(result.data()).isEmpty();
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
