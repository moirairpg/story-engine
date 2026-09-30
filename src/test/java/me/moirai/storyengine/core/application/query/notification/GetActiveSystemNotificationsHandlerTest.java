package me.moirai.storyengine.core.application.query.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.enums.NotificationLevel;
import me.moirai.storyengine.common.enums.NotificationType;
import me.moirai.storyengine.core.port.inbound.notification.GetActiveSystemNotifications;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetailsFixture;
import me.moirai.storyengine.core.port.outbound.adventure.InvitationReader;
import me.moirai.storyengine.core.port.outbound.adventure.PendingInvitationRow;
import me.moirai.storyengine.core.port.outbound.notification.ActiveSystemNotificationReader;

@ExtendWith(MockitoExtension.class)
public class GetActiveSystemNotificationsHandlerTest {

    @Mock
    private ActiveSystemNotificationReader reader;

    @Mock
    private InvitationReader invitationReader;

    @InjectMocks
    private GetActiveSystemNotificationsHandler handler;

    @Test
    public void shouldReturnActiveUnreadSystemNotificationsForUser() {

        // given
        var notifications = List.of(NotificationDetailsFixture.system());
        var query = new GetActiveSystemNotifications("some_user");

        when(reader.getActiveUnreadSystemNotifications("some_user")).thenReturn(notifications);

        // when
        var result = handler.execute(query);

        // then
        assertEquals(notifications, result);
    }

    @Test
    public void shouldMergePendingInvitationsWithNotificationsSortedByCreationDateDescending() {

        // given
        var older = new NotificationDetails(
                UUID.randomUUID(), "older", NotificationType.SYSTEM, NotificationLevel.INFO,
                List.of("some_user"), null, false, null,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"));

        var invitationRow = new PendingInvitationRow(
                UUID.randomUUID(), UUID.randomUUID(), "Dragon Hunt", "Alice Liddell", "some_user",
                Instant.parse("2026-06-01T00:00:00Z"));

        var query = new GetActiveSystemNotifications("some_user");

        when(reader.getActiveUnreadSystemNotifications("some_user")).thenReturn(List.of(older));
        when(invitationReader.getAllPendingByRecipient("some_user")).thenReturn(List.of(invitationRow));

        // when
        var result = handler.execute(query);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).creationDate()).isEqualTo(Instant.parse("2026-06-01T00:00:00Z"));
        assertThat(result.get(1).creationDate()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
    }

    @Test
    public void shouldMapPendingInvitationWithInviteMetadataAndInteractableFlag() {

        // given
        var invitationId = UUID.randomUUID();
        var adventureId = UUID.randomUUID();

        var invitationRow = new PendingInvitationRow(
                invitationId, adventureId, "Dragon Hunt", "Alice Liddell", "some_user",
                Instant.parse("2026-06-01T00:00:00Z"));

        var query = new GetActiveSystemNotifications("some_user");

        when(reader.getActiveUnreadSystemNotifications("some_user")).thenReturn(List.of());
        when(invitationReader.getAllPendingByRecipient("some_user")).thenReturn(List.of(invitationRow));

        // when
        var result = handler.execute(query);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().publicId()).isEqualTo(invitationId);
        assertThat(result.getFirst().isInteractable()).isTrue();
        assertThat(result.getFirst().message()).isEqualTo("Alice Liddell invited you to join Dragon Hunt");
        assertThat(result.getFirst().targetUsernames()).containsExactly("some_user");
        assertThat(result.getFirst().metadata()).containsOnlyKeys("kind", "adventureName");
        assertThat(result.getFirst().metadata()).containsEntry("kind", "ADVENTURE_INVITE");
        assertThat(result.getFirst().metadata()).containsEntry("adventureName", "Dragon Hunt");
    }

    @Test
    public void shouldReturnOnlyNotificationsWhenNoPendingInvitations() {

        // given
        var notifications = List.of(NotificationDetailsFixture.system());
        var query = new GetActiveSystemNotifications("some_user");

        when(reader.getActiveUnreadSystemNotifications("some_user")).thenReturn(notifications);
        when(invitationReader.getAllPendingByRecipient(anyString())).thenReturn(List.of());

        // when
        var result = handler.execute(query);

        // then
        assertThat(result).hasSize(1);
    }
}
