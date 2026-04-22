package me.moirai.storyengine.core.application.command.notification;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationFixture;
import me.moirai.storyengine.core.port.inbound.notification.ReadNotification;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;

@ExtendWith(MockitoExtension.class)
public class ReadNotificationHandlerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private ReadNotificationHandler handler;

    @Test
    public void shouldMarkAsReadWhenNotAlreadyRead() {

        // given
        var notification = NotificationFixture.broadcastWithId();
        var command = new ReadNotification(NotificationFixture.PUBLIC_ID, 1L);

        when(notificationRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(notification));

        // when
        handler.execute(command);

        // then
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    public void shouldSkipReadWhenAlreadyRead() {

        // given
        var notification = NotificationFixture.broadcastWithId();
        notification.markAsRead(1L);
        var command = new ReadNotification(NotificationFixture.PUBLIC_ID, 1L);

        when(notificationRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(notification));

        // when
        handler.execute(command);

        // then
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    public void shouldThrowWhenNotificationNotFound() {

        // given
        var command = new ReadNotification(NotificationFixture.PUBLIC_ID, 1L);

        when(notificationRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> handler.execute(command));
    }

    @Test
    public void shouldThrowWhenMarkAsReadViolatesBusinessRuleForUrgentBroadcast() {

        // given
        var notification = NotificationFixture.urgentBroadcastWithId();
        var command = new ReadNotification(NotificationFixture.PUBLIC_ID, 1L);

        when(notificationRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(notification));

        // then
        assertThrows(BusinessRuleViolationException.class, () -> handler.execute(command));
    }

    @Test
    public void shouldThrowWhenNotificationIdIsNull() {

        // given
        var command = new ReadNotification(null, 1L);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }
}
