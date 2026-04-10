package me.moirai.storyengine.core.application.query.world;

import static org.assertj.core.api.Assertions.assertThat;
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
import me.moirai.storyengine.common.enums.SearchView;
import me.moirai.storyengine.core.port.inbound.world.SearchWorlds;
import me.moirai.storyengine.core.port.outbound.world.WorldSearchReader;
import me.moirai.storyengine.core.port.outbound.world.WorldSearchRow;

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
                SearchView.MY_STUFF,
                null,
                null,
                1,
                2,
                1L);

        var expectedResult = PaginatedResult.<WorldSearchRow>of(List.of(), 0L, 1, 2);

        when(reader.search(any(SearchWorlds.class))).thenReturn(expectedResult);

        // When
        var result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.page()).isEqualTo(expectedResult.page());
        assertThat(result.items()).isEqualTo(expectedResult.items());
    }

    @Test
    public void searchWorlds_whenUserHasWritePermission_thenCanWriteIsTrue() {

        // Given
        var query = new SearchWorlds(null, SearchView.MY_STUFF, null, null, 1, 2, 1L);

        var row = new WorldSearchRow(UUID.randomUUID(), "name", "desc", "PUBLIC", Instant.now(), "WRITE");

        when(reader.search(any(SearchWorlds.class)))
                .thenReturn(PaginatedResult.of(List.of(row), 1L, 1, 2));

        // When
        var result = handler.handle(query);

        // Then
        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).canWrite()).isTrue();
    }

    @Test
    public void searchWorlds_whenUserHasNoPermission_thenCanWriteIsFalse() {

        // Given
        var query = new SearchWorlds(null, SearchView.EXPLORE, null, null, 1, 2, null);

        var row = new WorldSearchRow(UUID.randomUUID(), "name", "desc", "PUBLIC", Instant.now(), null);

        when(reader.search(any(SearchWorlds.class)))
                .thenReturn(PaginatedResult.of(List.of(row), 1L, 1, 2));

        // When
        var result = handler.handle(query);

        // Then
        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).canWrite()).isFalse();
    }
}
