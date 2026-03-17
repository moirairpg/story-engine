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

import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventures;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventuresResult;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@ExtendWith(MockitoExtension.class)
public class SearchAdventuresHandlerTest {

    @Mock
    private AdventureRepository repository;

    @InjectMocks
    private SearchAdventuresHandler handler;

    @Test
    public void searchAdventures() {

        // Given
        SearchAdventures query = new SearchAdventures(
                null,
                null,
                null,
                null,
                false,
                1,
                2,
                null,
                null,
                null,
                "name",
                "ASC",
                null,
                null,
                null);

        SearchAdventuresResult expectedResult = new SearchAdventuresResult(1, 0, 2, 0L, List.of());

        when(repository.search(any(SearchAdventures.class)))
                .thenReturn(expectedResult);

        // When
        SearchAdventuresResult result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).isEqualTo(expectedResult.items());
        assertThat(result.page()).isEqualTo(expectedResult.page());
    }
}
