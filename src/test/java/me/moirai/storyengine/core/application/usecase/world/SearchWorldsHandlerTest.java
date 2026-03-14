package me.moirai.storyengine.core.application.usecase.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.application.usecase.world.request.SearchWorlds;
import me.moirai.storyengine.core.application.usecase.world.result.SearchWorldsResult;
import me.moirai.storyengine.core.domain.world.WorldRepository;

@ExtendWith(MockitoExtension.class)
public class SearchWorldsHandlerTest {

    @Mock
    private WorldRepository repository;

    @InjectMocks
    private SearchWorldsHandler handler;

    @Test
    public void searchWorlds() {

        // Given
        SearchWorlds query = SearchWorlds.builder()
                .direction("ASC")
                .page(1)
                .size(2)
                .sortingField("name")
                .build();

        SearchWorldsResult expectedResult = SearchWorldsResult.builder()
                .page(1)
                .items(2)
                .build();

        when(repository.search(any(SearchWorlds.class)))
                .thenReturn(expectedResult);

        // When
        SearchWorldsResult result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEqualTo(expectedResult.getItems());
        assertThat(result.getPage()).isEqualTo(expectedResult.getPage());
    }
}
