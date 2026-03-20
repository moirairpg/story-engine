package me.moirai.storyengine.core.application.query.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.enums.SearchView;
import me.moirai.storyengine.core.port.inbound.world.SearchWorlds;
import me.moirai.storyengine.core.port.inbound.world.WorldSummary;
import me.moirai.storyengine.core.port.outbound.world.WorldSearchReader;

@ExtendWith(MockitoExtension.class)
public class SearchWorldsHandlerTest {

    @Mock
    private WorldSearchReader reader;

    @InjectMocks
    private SearchWorldsHandler handler;

    @Test
    public void searchWorlds_whenValidQuery_thenReturnResult() {

        // Given
        var query = new SearchWorlds(
                null,
                null,
                SearchView.MY_STUFF,
                null,
                null,
                1,
                2,
                "requesterId");

        var expectedResult = PaginatedResult.<WorldSummary>of(List.of(), 0L, 1, 2);

        when(reader.search(any(SearchWorlds.class))).thenReturn(expectedResult);

        // When
        var result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.page()).isEqualTo(expectedResult.page());
        assertThat(result.items()).isEqualTo(expectedResult.items());
    }
}
