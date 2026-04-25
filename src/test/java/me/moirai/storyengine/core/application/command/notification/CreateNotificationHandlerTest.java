package me.moirai.storyengine.core.application.command.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.application.event.notification.NotificationCreated;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationFixture;
import me.moirai.storyengine.core.domain.notification.NotificationLevel;
import me.moirai.storyengine.core.domain.notification.NotificationType;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.inbound.notification.CreateNotification;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@ExtendWith(MockitoExtension.class)
public class CreateNotificationHandlerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CreateNotificationHandler handler;

    @Test
    public void shouldReturnNotificationDetailsWithAllRecipientsWhenSystemWithMultipleTargets() {

        // given
        var command = new CreateNotification(
                "Hello",
                NotificationType.SYSTEM,
                NotificationLevel.INFO,
                List.of("alice", "bob", "charlie"),
                false,
                null);

        var alice = userWith(10L, "alice");
        var bob = userWith(20L, "bob");
        var charlie = userWith(30L, "charlie");

        var saved = NotificationFixture.systemWithId();

        when(userRepository.findAllByUsernameIn(anyCollection())).thenReturn(List.of(alice, bob, charlie));
        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        // when
        var result = handler.execute(command);

        // then
        assertNotNull(result);
        assertEquals(3, result.targetUsernames().size());
        assertTrue(result.targetUsernames().containsAll(List.of("alice", "bob", "charlie")));
    }

    @Test
    public void shouldSaveSingleAggregateWhenSystemWithMultipleTargets() {

        // given
        var command = new CreateNotification(
                "Hello",
                NotificationType.SYSTEM,
                NotificationLevel.INFO,
                List.of("alice", "bob", "charlie"),
                false,
                null);

        var alice = userWith(10L, "alice");
        var bob = userWith(20L, "bob");
        var charlie = userWith(30L, "charlie");

        var saved = NotificationFixture.systemWithId();

        when(userRepository.findAllByUsernameIn(anyCollection())).thenReturn(List.of(alice, bob, charlie));
        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        // when
        handler.execute(command);

        // then
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    public void shouldPublishSingleEventWhenSystemWithMultipleTargets() {

        // given
        var command = new CreateNotification(
                "Hello",
                NotificationType.SYSTEM,
                NotificationLevel.INFO,
                List.of("alice", "bob", "charlie"),
                false,
                null);

        var alice = userWith(10L, "alice");
        var bob = userWith(20L, "bob");
        var charlie = userWith(30L, "charlie");

        var saved = NotificationFixture.systemWithId();

        when(userRepository.findAllByUsernameIn(anyCollection())).thenReturn(List.of(alice, bob, charlie));
        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        // when
        handler.execute(command);

        // then
        var captor = ArgumentCaptor.forClass(NotificationCreated.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        assertThat(captor.getValue().publicId()).isEqualTo(saved.getPublicId());
    }

    @Test
    public void shouldThrowIllegalArgumentWhenSystemHasNoTargets() {

        // given
        var command = new CreateNotification(
                "Hello",
                NotificationType.SYSTEM,
                NotificationLevel.INFO,
                List.of(),
                false,
                null);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.validate(command));
    }

    @Test
    public void shouldThrowIllegalArgumentWhenBroadcastHasTargets() {

        // given
        var command = new CreateNotification(
                "Hello",
                NotificationType.BROADCAST,
                NotificationLevel.INFO,
                List.of("alice"),
                false,
                null);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.validate(command));
    }

    @Test
    public void shouldThrowIllegalArgumentWhenTypeIsGame() {

        // given
        var command = new CreateNotification(
                "Hello",
                NotificationType.GAME,
                null,
                null,
                false,
                null);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.validate(command));
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenTargetUsernameDoesNotExist() {

        // given
        var command = new CreateNotification(
                "Hello",
                NotificationType.SYSTEM,
                NotificationLevel.INFO,
                List.of("john.doe", "ghost.user"),
                false,
                null);

        when(userRepository.findAllByUsernameIn(anyCollection()))
                .thenReturn(List.of(UserFixture.playerWithId()));

        // then
        assertThrows(NotFoundException.class, () -> handler.execute(command));
    }

    @Test
    public void shouldReturnEmptyTargetUsernamesWhenBroadcast() {

        // given
        var command = new CreateNotification(
                "Hello",
                NotificationType.BROADCAST,
                NotificationLevel.INFO,
                null,
                false,
                null);

        var saved = NotificationFixture.broadcastWithId();

        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        // when
        var result = handler.execute(command);

        // then
        assertNotNull(result);
        assertTrue(result.targetUsernames().isEmpty());
    }

    private User userWith(Long id, String username) {

        var user = User.builder()
                .discordId("discord-" + id)
                .username(username)
                .role(me.moirai.storyengine.common.enums.Role.PLAYER)
                .build();

        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
