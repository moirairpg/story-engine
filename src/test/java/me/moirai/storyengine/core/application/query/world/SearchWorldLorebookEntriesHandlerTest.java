package me.moirai.storyengine.core.application.query.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.core.port.inbound.LorebookEntrySummary;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntries;
import me.moirai.storyengine.core.port.outbound.world.WorldLorebookSearchReader;

@ExtendWith(MockitoExtension.class)
public class SearchWorldLorebookEntriesHandlerTest {

    @Mock
    private WorldLorebookSearchReader reader;

    @InjectMocks
    private SearchWorldLorebookEntriesHandler handler;

    @Test
    public void searchEntries_whenValidRequest_thenReturnEntries() {

        // Given
        var query = new SearchWorldLorebookEntries(
                UUID.randomUUID(),
                null,
                null,
                null,
                1,
                2,
                "requesterId");

        var expectedResult = PaginatedResult.<LorebookEntrySummary>of(List.of(), 0L, 1, 2);

        when(reader.search(any(SearchWorldLorebookEntries.class))).thenReturn(expectedResult);

        // When
        var result = handler.execute(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.page()).isEqualTo(expectedResult.page());
        assertThat(result.items()).isEqualTo(expectedResult.items());
    }
}
