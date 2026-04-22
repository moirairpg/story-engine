package me.moirai.storyengine.core.application.command.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationCreated;
import me.moirai.storyengine.core.domain.notification.NotificationFixture;
import me.moirai.storyengine.core.domain.notification.NotificationLevel;
import me.moirai.storyengine.core.domain.notification.NotificationType;
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
    public void shouldCreateBroadcastNotificationAndPublishEventWhenValidCommand() {

        // given
        var command = new CreateNotification(
                "Hello",
                NotificationType.BROADCAST,
                NotificationLevel.INFO,
                null, null, false, null);
        var saved = NotificationFixture.broadcastWithId();

        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        // when
        var result = handler.execute(command);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(saved.getPublicId(), result.get(0).publicId());
        verify(notificationRepository).save(any(Notification.class));
        verify(eventPublisher).publishEvent(any(NotificationCreated.class));
    }

    @Test
    public void shouldCreateOneSystemNotificationPerResolvedUsername() {

        // given
        var command = new CreateNotification(
                "Hello",
                NotificationType.SYSTEM,
                NotificationLevel.INFO,
                List.of("john.doe", "jane.doe"), null, false, null);
        var user = UserFixture.playerWithId();
        var saved = NotificationFixture.systemWithId();

        when(userRepository.findByUsername(eq("john.doe"))).thenReturn(Optional.of(user));
        when(userRepository.findByUsername(eq("jane.doe"))).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        // when
        var result = handler.execute(command);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(eventPublisher, times(2)).publishEvent(any(NotificationCreated.class));
    }

    @Test
    public void shouldThrowWhenAnyUsernameIsUnknown() {

        // given
        var command = new CreateNotification(
                "Hello",
                NotificationType.SYSTEM,
                NotificationLevel.INFO,
                List.of("john.doe", "ghost.user"), null, false, null);

        when(userRepository.findByUsername(eq("john.doe"))).thenReturn(Optional.of(UserFixture.playerWithId()));
        when(userRepository.findByUsername(eq("ghost.user"))).thenReturn(Optional.empty());

        // then
        assertThrows(BusinessRuleViolationException.class, () -> handler.execute(command));
    }

    @Test
    public void shouldThrowWhenMessageIsNullOrBlank() {

        // given
        var command = new CreateNotification(
                " ",
                NotificationType.BROADCAST,
                NotificationLevel.INFO,
                null, null, false, null);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.validate(command));
    }

    @Test
    public void shouldThrowWhenTypeIsGame() {

        // given
        var command = new CreateNotification(
                "Hello",
                NotificationType.GAME,
                null,
                null, null, false, null);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.validate(command));
    }

    @Test
    public void shouldThrowWhenSystemNotificationHasEmptyTargetUsernames() {

        // given
        var command = new CreateNotification(
                "Hello",
                NotificationType.SYSTEM,
                NotificationLevel.INFO,
                List.of(), null, false, null);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.validate(command));
    }
}
