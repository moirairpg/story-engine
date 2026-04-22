package me.moirai.storyengine.core.application.query.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.port.inbound.notification.GetActiveSystemNotifications;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetailsFixture;
import me.moirai.storyengine.core.port.outbound.notification.ActiveSystemNotificationReader;

@ExtendWith(MockitoExtension.class)
public class GetActiveSystemNotificationsHandlerTest {

    @Mock
    private ActiveSystemNotificationReader reader;

    @InjectMocks
    private GetActiveSystemNotificationsHandler handler;

    @Test
    public void shouldReturnActiveUnreadSystemNotificationsForUser() {

        // given
        var notifications = List.of(NotificationDetailsFixture.system());
        var query = new GetActiveSystemNotifications(1L);

        when(reader.getActiveUnreadSystemNotifications(any(Long.class))).thenReturn(notifications);

        // when
        var result = handler.execute(query);

        // then
        assertEquals(notifications, result);
    }
}
