package me.moirai.storyengine.core.application.query.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.enums.NotificationStatus;
import me.moirai.storyengine.core.domain.notification.NotificationLevel;
import me.moirai.storyengine.core.domain.notification.NotificationType;
import me.moirai.storyengine.core.port.inbound.notification.SearchNotifications;
import me.moirai.storyengine.core.port.outbound.notification.NotificationSearchReader;
import me.moirai.storyengine.core.port.outbound.notification.NotificationSearchRow;

@ExtendWith(MockitoExtension.class)
public class SearchNotificationsHandlerTest {

    @Mock
    private NotificationSearchReader reader;

    @InjectMocks
    private SearchNotificationsHandler handler;

    @Test
    public void shouldReturnPaginatedResultWhenSearchIsExecuted() {

        // given
        var row = new NotificationSearchRow(
                UUID.randomUUID(),
                "Message",
                NotificationType.BROADCAST,
                NotificationLevel.INFO,
                NotificationStatus.UNREAD,
                Instant.parse("2026-01-01T00:00:00Z"));

        var rows = new PaginatedResult<>(List.of(row), 1L, 1L, 1, 1);

        var query = new SearchNotifications(null, null, null, null, 1L, false, null, null, 1, 10);

        when(reader.search(any(SearchNotifications.class))).thenReturn(rows);

        // when
        var result = handler.execute(query);

        // then
        assertEquals(1, result.data().size());
        assertEquals("Message", result.data().get(0).message());
    }

    @Test
    public void shouldReturnEmptyPageWhenNoResultsMatch() {

        // given
        var rows = new PaginatedResult<NotificationSearchRow>(List.of(), 0L, 0L, 1, 1);
        var query = new SearchNotifications(null, null, null, null, 1L, false, null, null, 1, 10);

        when(reader.search(any(SearchNotifications.class))).thenReturn(rows);

        // when
        var result = handler.execute(query);

        // then
        assertTrue(result.data().isEmpty());
    }
}
