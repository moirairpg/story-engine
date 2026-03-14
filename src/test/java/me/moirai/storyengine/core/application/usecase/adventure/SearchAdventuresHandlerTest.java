package me.moirai.storyengine.core.application.usecase.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
        SearchAdventures query = SearchAdventures.builder()
                .direction("ASC")
                .page(1)
                .size(2)
                .sortingField("name")
                .build();

        SearchAdventuresResult expectedResult = SearchAdventuresResult.builder()
                .page(1)
                .items(2)
                .build();

        when(repository.search(any(SearchAdventures.class)))
                .thenReturn(expectedResult);

        // When
        SearchAdventuresResult result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEqualTo(expectedResult.getItems());
        assertThat(result.getPage()).isEqualTo(expectedResult.getPage());
    }
}