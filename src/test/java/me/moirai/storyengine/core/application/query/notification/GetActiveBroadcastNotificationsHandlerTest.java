package me.moirai.storyengine.core.application.query.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.port.inbound.notification.GetActiveBroadcastNotifications;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetailsFixture;
import me.moirai.storyengine.core.port.outbound.notification.ActiveBroadcastNotificationReader;

@ExtendWith(MockitoExtension.class)
public class GetActiveBroadcastNotificationsHandlerTest {

    @Mock
    private ActiveBroadcastNotificationReader reader;

    @InjectMocks
    private GetActiveBroadcastNotificationsHandler handler;

    @Test
    public void shouldReturnActiveBroadcastsFromReader() {

        // given
        var broadcasts = List.of(NotificationDetailsFixture.broadcast());
        var query = new GetActiveBroadcastNotifications(1L);

        when(reader.getActiveBroadcasts(1L)).thenReturn(broadcasts);

        // when
        var result = handler.execute(query);

        // then
        assertEquals(broadcasts, result);
    }
}
