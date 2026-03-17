package me.moirai.storyengine.core.application.query.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.port.inbound.world.SearchWorlds;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldsResult;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@ExtendWith(MockitoExtension.class)
public class SearchWorldsHandlerTest {

    @Mock
    private WorldRepository repository;

    @InjectMocks
    private SearchWorldsHandler handler;

    @Test
    public void searchWorlds() {

        // Given
        SearchWorlds query = new SearchWorlds(
                null,
                null,
                1,
                2,
                "name",
                "ASC",
                null,
                null,
                null);

        SearchWorldsResult expectedResult = new SearchWorldsResult(
                1,
                2,
                0,
                0,
                null);

        when(repository.search(any(SearchWorlds.class)))
                .thenReturn(expectedResult);

        // When
        SearchWorldsResult result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).isEqualTo(expectedResult.items());
        assertThat(result.page()).isEqualTo(expectedResult.page());
    }
}
