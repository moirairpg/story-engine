package me.moirai.storyengine.core.application.query.adventure;

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
import me.moirai.storyengine.core.port.inbound.adventure.AdventureSummary;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventures;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureSearchReader;

@ExtendWith(MockitoExtension.class)
public class SearchAdventuresHandlerTest {

    @Mock
    private AdventureSearchReader reader;

    @InjectMocks
    private SearchAdventuresHandler handler;

    @Test
    public void searchAdventures_whenValidRequest_thenReturnResults() {

        // Given
        var query = new SearchAdventures(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                SearchView.MY_STUFF,
                null,
                null,
                1,
                2,
                "requesterId");

        var expectedResult = PaginatedResult.<AdventureSummary>of(List.of(), 0L, 1, 2);

        when(reader.search(any(SearchAdventures.class))).thenReturn(expectedResult);

        // When
        var result = handler.execute(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.page()).isEqualTo(expectedResult.page());
        assertThat(result.items()).isEqualTo(expectedResult.items());
    }
}
