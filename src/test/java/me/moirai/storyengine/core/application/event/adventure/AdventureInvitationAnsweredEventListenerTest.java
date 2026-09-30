package me.moirai.storyengine.core.application.event.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.application.event.notification.NotificationCreatedEvent;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureInvitationAnsweredEvent;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.common.enums.NotificationLevel;
import me.moirai.storyengine.common.enums.NotificationType;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AdventureInvitationAnsweredEventListenerTest {

    private static final Long RESPONDER_ID = 10L;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private AdventureRepository adventureRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AdventureInvitationAnsweredEventListener listener;

    private AdventureInvitationAnsweredEvent acceptedEvent() {
        var adventure = AdventureFixture.privateAdventureWithId();
        var invitation = adventure.invite(RESPONDER_ID, AdventureFixture.OWNER_ID);
        adventure.acceptInvitation(invitation.getPublicId(), 1L, RESPONDER_ID);
        return drain(adventure);
    }

    private AdventureInvitationAnsweredEvent declinedEvent() {
        var adventure = AdventureFixture.privateAdventureWithId();
        var invitation = adventure.invite(RESPONDER_ID, AdventureFixture.OWNER_ID);
        adventure.declineInvitation(invitation.getPublicId());
        return drain(adventure);
    }

    private AdventureInvitationAnsweredEvent drain(Adventure adventure) {
        return (AdventureInvitationAnsweredEvent) adventure.drainEvents().stream()
                .filter(AdventureInvitationAnsweredEvent.class::isInstance)
                .findFirst()
                .orElseThrow();
    }

    private User responder() {
        var user = UserFixture.player().username("bob").displayName("Bob the Brave").build();
        ReflectionTestUtils.setField(user, "id", RESPONDER_ID);
        return user;
    }

    private Notification savedNotification() {
        var notification = Notification.builder()
                .type(NotificationType.SYSTEM)
                .level(NotificationLevel.INFO)
                .message("stub")
                .isInteractable(false)
                .recipientUserIds(List.of(100L))
                .build();
        ReflectionTestUtils.setField(notification, "publicId", UUID.randomUUID());
        return notification;
    }

    @Test
    public void shouldNotifyManagersWithAnAcceptedMessageAndPublishNotificationCreatedEvent() {

        // given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(responder()));
        when(adventureRepository.findManagerUserIdsByAdventureId(anyLong())).thenReturn(List.of(100L));
        when(notificationRepository.save(any())).thenReturn(savedNotification());

        // when
        listener.onAdventureInvitationAnswered(acceptedEvent());

        // then
        var captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getMessage()).contains("Bob the Brave accepted");
        assertThat(captor.getValue().getMetadata()).containsEntry("kind", "ADVENTURE_INVITE_RESPONSE");
        assertThat(captor.getValue().getMetadata()).containsOnlyKeys("kind", "adventureId");
        verify(eventPublisher).publishEvent(any(NotificationCreatedEvent.class));
    }

    @Test
    public void shouldNotifyManagersWithADeclinedMessage() {

        // given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(responder()));
        when(adventureRepository.findManagerUserIdsByAdventureId(anyLong())).thenReturn(List.of(100L));
        when(notificationRepository.save(any())).thenReturn(savedNotification());

        // when
        listener.onAdventureInvitationAnswered(declinedEvent());

        // then
        var captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getMessage()).contains("Bob the Brave declined");
    }

    @Test
    public void shouldThrowWhenResponderIsMissing() {

        // given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> listener.onAdventureInvitationAnswered(acceptedEvent()))
                .isInstanceOf(NotFoundException.class);
        verify(notificationRepository, never()).save(any());
    }
}
