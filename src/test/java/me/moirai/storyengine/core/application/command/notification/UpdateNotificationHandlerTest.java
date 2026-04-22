package me.moirai.storyengine.core.application.command.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationFixture;
import me.moirai.storyengine.core.domain.notification.NotificationLevel;
import me.moirai.storyengine.core.port.inbound.notification.UpdateNotification;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;

@ExtendWith(MockitoExtension.class)
public class UpdateNotificationHandlerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private UpdateNotificationHandler handler;

    @Test
    public void shouldUpdateNotificationWhenFound() {

        // given
        var existing = NotificationFixture.broadcastWithId();
        var command = new UpdateNotification(
                NotificationFixture.PUBLIC_ID,
                "Updated message",
                NotificationLevel.URGENT,
                1L);

        when(notificationRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(existing));
        when(notificationRepository.save(any(Notification.class))).thenReturn(existing);

        // when
        var result = handler.execute(command);

        // then
        assertEquals("Updated message", result.message());
        assertEquals(NotificationLevel.URGENT, result.level());
    }

    @Test
    public void shouldThrowWhenNotificationNotFound() {

        // given
        var command = new UpdateNotification(
                NotificationFixture.PUBLIC_ID,
                "Updated message",
                NotificationLevel.URGENT,
                1L);

        when(notificationRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> handler.execute(command));
    }

    @Test
    public void shouldThrowWhenNotificationIdIsNull() {

        // given
        var command = new UpdateNotification(null, "message", NotificationLevel.INFO, 1L);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }
}
