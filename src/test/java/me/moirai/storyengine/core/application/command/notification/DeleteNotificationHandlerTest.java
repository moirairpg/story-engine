package me.moirai.storyengine.core.application.command.notification;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.notification.NotificationFixture;
import me.moirai.storyengine.core.port.inbound.notification.DeleteNotification;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;

@ExtendWith(MockitoExtension.class)
public class DeleteNotificationHandlerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private DeleteNotificationHandler handler;

    @Test
    public void shouldDeleteNotificationWhenFound() {

        // given
        var notification = NotificationFixture.broadcastWithId();
        var command = new DeleteNotification(NotificationFixture.PUBLIC_ID);

        when(notificationRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(notification));

        // when
        handler.execute(command);

        // then
        verify(notificationRepository).deleteByPublicId(NotificationFixture.PUBLIC_ID);
    }

    @Test
    public void shouldThrowWhenNotificationNotFound() {

        // given
        var command = new DeleteNotification(NotificationFixture.PUBLIC_ID);

        when(notificationRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> handler.execute(command));
    }

    @Test
    public void shouldThrowWhenNotificationIdIsNull() {

        // given
        var command = new DeleteNotification(null);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }
}
