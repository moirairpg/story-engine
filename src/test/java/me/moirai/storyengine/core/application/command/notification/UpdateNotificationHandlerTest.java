package me.moirai.storyengine.core.application.command.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationFixture;
import me.moirai.storyengine.core.domain.notification.NotificationLevel;
import me.moirai.storyengine.core.domain.notification.NotificationType;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.port.inbound.notification.UpdateNotification;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UpdateNotificationHandlerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UpdateNotificationHandler handler;

    @Test
    public void shouldReturnDetailsWithAllRecipientUsernamesWhenSystemNotificationUpdated() {

        // given
        var existing = Notification.builder()
                .message("System message")
                .type(NotificationType.SYSTEM)
                .level(NotificationLevel.INFO)
                .recipientUserId(10L)
                .recipientUserId(20L)
                .build();

        ReflectionTestUtils.setField(existing, "id", 100L);
        ReflectionTestUtils.setField(existing, "publicId", NotificationFixture.PUBLIC_ID);

        var command = new UpdateNotification(
                NotificationFixture.PUBLIC_ID,
                "Updated message",
                NotificationLevel.URGENT);

        var alice = userWith(10L, "alice");
        var bob = userWith(20L, "bob");

        when(notificationRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(existing));
        when(notificationRepository.save(any(Notification.class))).thenReturn(existing);
        when(userRepository.findAllById(anyCollection())).thenReturn(List.of(alice, bob));

        // when
        var result = handler.execute(command);

        // then
        assertEquals(2, result.targetUsernames().size());
        assertTrue(result.targetUsernames().containsAll(List.of("alice", "bob")));
    }

    @Test
    public void shouldReturnEmptyTargetUsernamesWhenBroadcastUpdated() {

        // given
        var existing = NotificationFixture.broadcastWithId();
        var command = new UpdateNotification(
                NotificationFixture.PUBLIC_ID,
                "Updated message",
                NotificationLevel.URGENT);

        when(notificationRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(existing));
        when(notificationRepository.save(any(Notification.class))).thenReturn(existing);

        // when
        var result = handler.execute(command);

        // then
        assertTrue(result.targetUsernames().isEmpty());
    }

    @Test
    public void shouldThrowExceptionWhenNotificationNotFound() {

        // given
        var command = new UpdateNotification(
                NotificationFixture.PUBLIC_ID,
                "Updated message",
                NotificationLevel.URGENT);

        when(notificationRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> handler.execute(command));
    }

    @Test
    public void shouldThrowWhenNotificationIdIsNull() {

        // given
        var command = new UpdateNotification(null, "message", NotificationLevel.INFO);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
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
