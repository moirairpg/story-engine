package me.moirai.storyengine.core.application.query.notification;

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
import me.moirai.storyengine.core.port.inbound.notification.GetNotification;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetailsFixture;
import me.moirai.storyengine.core.port.outbound.notification.NotificationReader;

@ExtendWith(MockitoExtension.class)
public class GetNotificationHandlerTest {

    @Mock
    private NotificationReader reader;

    @InjectMocks
    private GetNotificationHandler handler;

    @Test
    public void shouldReturnNotificationDetailsWhenFound() {

        // given
        var details = NotificationDetailsFixture.broadcast();
        var query = new GetNotification(UUID.randomUUID(), 1L, true);

        when(reader.getNotificationByPublicId(any(UUID.class), any(Long.class), any(Boolean.class)))
                .thenReturn(Optional.of(details));

        // when
        var result = handler.execute(query);

        // then
        assertEquals(details, result);
    }

    @Test
    public void shouldThrowWhenNotificationNotFound() {

        // given
        var query = new GetNotification(UUID.randomUUID(), 1L, false);

        when(reader.getNotificationByPublicId(any(UUID.class), any(Long.class), any(Boolean.class)))
                .thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> handler.execute(query));
    }

    @Test
    public void shouldThrowWhenNotificationIdIsNull() {

        // given
        var query = new GetNotification(null, 1L, false);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }
}
