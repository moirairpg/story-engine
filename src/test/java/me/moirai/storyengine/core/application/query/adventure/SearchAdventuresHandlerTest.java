package me.moirai.storyengine.core.application.query.adventure;

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
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventures;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureSearchReader;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureSearchRow;

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
                SearchView.MY_STUFF,
                null,
                null,
                1,
                2,
                1L);

        var expectedResult = PaginatedResult.<AdventureSearchRow>of(List.of(), 0L, 1, 2);

        when(reader.search(any(SearchAdventures.class))).thenReturn(expectedResult);

        // When
        var result = handler.execute(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.page()).isEqualTo(expectedResult.page());
        assertThat(result.items()).isEqualTo(expectedResult.items());
    }

    @Test
    public void searchAdventures_whenUserHasOwnerPermission_thenCanWriteIsTrue() {

        // Given
        var query = new SearchAdventures(
                null, null, null, null, null, null, null,
                SearchView.MY_STUFF, null, null, 1, 2, 1L);

        var row = new AdventureSearchRow(
                UUID.randomUUID(), "name", "desc", "world", "persona", "PUBLIC", Instant.now(), "OWNER");

        when(reader.search(any(SearchAdventures.class)))
                .thenReturn(PaginatedResult.of(List.of(row), 1L, 1, 2));

        // When
        var result = handler.execute(query);

        // Then
        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).canWrite()).isTrue();
    }

    @Test
    public void searchAdventures_whenUserHasReadPermission_thenCanWriteIsFalse() {

        // Given
        var query = new SearchAdventures(
                null, null, null, null, null, null, null,
                SearchView.EXPLORE, null, null, 1, 2, null);

        var row = new AdventureSearchRow(
                UUID.randomUUID(), "name", "desc", "world", "persona", "PUBLIC", Instant.now(), "READ");

        when(reader.search(any(SearchAdventures.class)))
                .thenReturn(PaginatedResult.of(List.of(row), 1L, 1, 2));

        // When
        var result = handler.execute(query);

        // Then
        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).canWrite()).isFalse();
    }

    @Test
    public void searchAdventures_whenUserHasNoPermission_thenCanWriteIsFalse() {

        // Given
        var query = new SearchAdventures(
                null, null, null, null, null, null, null,
                SearchView.EXPLORE, null, null, 1, 2, null);

        var row = new AdventureSearchRow(
                UUID.randomUUID(), "name", "desc", "world", "persona", "PUBLIC", Instant.now(), null);

        when(reader.search(any(SearchAdventures.class)))
                .thenReturn(PaginatedResult.of(List.of(row), 1L, 1, 2));

        // When
        var result = handler.execute(query);

        // Then
        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).canWrite()).isFalse();
    }
}
