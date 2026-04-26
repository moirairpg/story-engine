package me.moirai.storyengine.infrastructure.event.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.application.event.notification.NotificationCreated;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationFixture;
import me.moirai.storyengine.core.domain.notification.NotificationLevel;
import me.moirai.storyengine.core.domain.notification.NotificationType;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdventureRepository adventureRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationEventListener listener;

    @Test
    void shouldSendSystemNotificationToEachRecipientWhenTypeIsSystem() {

        // given
        var alice = userWith(10L, "alice");
        var bob = userWith(20L, "bob");
        var charlie = userWith(30L, "charlie");

        var notification = systemNotificationWith(List.of(10L, 20L, 30L));
        var event = new NotificationCreated(NotificationFixture.PUBLIC_ID);

        when(notificationRepository.findByPublicId(eq(NotificationFixture.PUBLIC_ID)))
                .thenReturn(Optional.of(notification));
        when(userRepository.findAllById(anyCollection())).thenReturn(List.of(alice, bob, charlie));

        // when
        listener.onNotificationCreated(event);

        // then
        verify(messagingTemplate, times(3)).convertAndSendToUser(any(String.class),
                eq("/queue/notifications/system"), any(NotificationDetails.class));
    }

    @Test
    void shouldDeliverOnlyTheRecipientsOwnUsernameInSystemPayload() {

        // given
        var alice = userWith(10L, "alice");
        var bob = userWith(20L, "bob");
        var charlie = userWith(30L, "charlie");

        var notification = systemNotificationWith(List.of(10L, 20L, 30L));
        var event = new NotificationCreated(NotificationFixture.PUBLIC_ID);

        when(notificationRepository.findByPublicId(eq(NotificationFixture.PUBLIC_ID)))
                .thenReturn(Optional.of(notification));
        when(userRepository.findAllById(anyCollection())).thenReturn(List.of(alice, bob, charlie));

        var usernameCaptor = ArgumentCaptor.forClass(String.class);
        var payloadCaptor = ArgumentCaptor.forClass(NotificationDetails.class);

        // when
        listener.onNotificationCreated(event);

        // then
        verify(messagingTemplate, times(3)).convertAndSendToUser(
                usernameCaptor.capture(),
                eq("/queue/notifications/system"),
                payloadCaptor.capture());

        var deliveries = usernameCaptor.getAllValues();
        var payloads = payloadCaptor.getAllValues();

        for (var i = 0; i < deliveries.size(); i++) {
            assertThat(payloads.get(i).targetUsernames()).containsExactly(deliveries.get(i));
        }
    }

    @Test
    void shouldSendSingleBroadcastWhenTypeIsBroadcast() {

        // given
        var notification = NotificationFixture.broadcastWithId();
        var event = new NotificationCreated(NotificationFixture.PUBLIC_ID);

        when(notificationRepository.findByPublicId(eq(NotificationFixture.PUBLIC_ID)))
                .thenReturn(Optional.of(notification));

        var payloadCaptor = ArgumentCaptor.forClass(NotificationDetails.class);

        // when
        listener.onNotificationCreated(event);

        // then
        verify(messagingTemplate, times(1)).convertAndSend(
                eq("/topic/notifications/broadcast"),
                payloadCaptor.capture());

        assertThat(payloadCaptor.getValue().targetUsernames()).isEmpty();
    }

    @Test
    void shouldSendAdventureNotificationWhenTypeIsGame() {

        // given
        var notification = NotificationFixture.gameWithId();
        var event = new NotificationCreated(NotificationFixture.PUBLIC_ID);

        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", NotificationFixture.ADVENTURE_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        when(notificationRepository.findByPublicId(eq(NotificationFixture.PUBLIC_ID)))
                .thenReturn(Optional.of(notification));
        when(adventureRepository.findById(NotificationFixture.ADVENTURE_ID))
                .thenReturn(Optional.of(adventure));

        // when
        listener.onNotificationCreated(event);

        // then
        verify(messagingTemplate, times(1)).convertAndSend(
                eq("/topic/notifications/adventure/" + AdventureFixture.PUBLIC_ID),
                any(NotificationDetails.class));
    }

    @Test
    void shouldThrowNotFoundWhenAnyRecipientUserMissing() {

        // given
        var alice = userWith(10L, "alice");

        var notification = systemNotificationWith(List.of(10L, 20L));
        var event = new NotificationCreated(NotificationFixture.PUBLIC_ID);

        when(notificationRepository.findByPublicId(eq(NotificationFixture.PUBLIC_ID)))
                .thenReturn(Optional.of(notification));
        when(userRepository.findAllById(anyCollection())).thenReturn(List.of(alice));

        // then
        assertThatThrownBy(() -> listener.onNotificationCreated(event))
                .isInstanceOf(NotFoundException.class);
    }

    private Notification systemNotificationWith(List<Long> recipientIds) {

        var builder = Notification.builder()
                .message("System message")
                .type(NotificationType.SYSTEM)
                .level(NotificationLevel.INFO);

        for (var id : recipientIds) {
            builder.recipientUserId(id);
        }

        var notification = builder.build();
        ReflectionTestUtils.setField(notification, "id", NotificationFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(notification, "publicId", NotificationFixture.PUBLIC_ID);
        return notification;
    }

    private User userWith(Long id, String username) {

        var user = User.builder()
                .discordId("discord-" + id)
                .username(username)
                .role(Role.PLAYER)
                .build();

        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
