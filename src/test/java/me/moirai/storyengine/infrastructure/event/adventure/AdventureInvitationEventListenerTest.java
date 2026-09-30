package me.moirai.storyengine.infrastructure.event.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.UserInvitedToAdventureEvent;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.outbound.adventure.InvitationReader;
import me.moirai.storyengine.core.port.outbound.adventure.PendingInvitationRow;

@ExtendWith(MockitoExtension.class)
public class AdventureInvitationEventListenerTest {

    @Mock
    private InvitationReader invitationReader;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private AdventureInvitationEventListener listener;

    private UserInvitedToAdventureEvent eventFor(UUID invitationId) {
        var adventure = AdventureFixture.privateAdventureWithId();
        var invitation = adventure.invite(1L, AdventureFixture.OWNER_ID);
        ReflectionTestUtils.setField(invitation, "publicId", invitationId);
        return (UserInvitedToAdventureEvent) adventure.drainEvents().stream()
                .filter(UserInvitedToAdventureEvent.class::isInstance)
                .findFirst()
                .orElseThrow();
    }

    @Test
    public void shouldPushTheInvitationToTheRecipientWhenPending() {

        // given
        var invitationId = UUID.randomUUID();
        var row = new PendingInvitationRow(
                invitationId, UUID.randomUUID(), "Dragon Hunt", "Alice Liddell", "bob", Instant.now());

        when(invitationReader.getPendingByPublicId(any())).thenReturn(Optional.of(row));

        // when
        listener.onUserInvitedToAdventure(eventFor(invitationId));

        // then
        verify(messagingTemplate).convertAndSendToUser(
                eq("bob"), eq("/queue/notifications/system"), any(NotificationDetails.class));
    }

    @Test
    public void shouldNameTheInviterByDisplayNameAndCarryNoNameInTheMetadataWhenPending() {

        // given
        var invitationId = UUID.randomUUID();
        var row = new PendingInvitationRow(
                invitationId, UUID.randomUUID(), "Dragon Hunt", "Alice Liddell", "bob", Instant.now());
        var captor = ArgumentCaptor.forClass(NotificationDetails.class);

        when(invitationReader.getPendingByPublicId(any())).thenReturn(Optional.of(row));

        // when
        listener.onUserInvitedToAdventure(eventFor(invitationId));

        // then
        verify(messagingTemplate).convertAndSendToUser(
                eq("bob"), eq("/queue/notifications/system"), captor.capture());

        assertThat(captor.getValue().message()).isEqualTo("Alice Liddell invited you to join Dragon Hunt");
        assertThat(captor.getValue().metadata()).containsOnlyKeys("kind", "adventureName");
    }

    @Test
    public void shouldSendNothingWhenTheInvitationIsNoLongerPending() {

        // given
        when(invitationReader.getPendingByPublicId(any())).thenReturn(Optional.empty());

        // when
        listener.onUserInvitedToAdventure(eventFor(UUID.randomUUID()));

        // then
        verify(messagingTemplate, never()).convertAndSendToUser(
                any(String.class), any(String.class), any(Object.class));
    }
}
