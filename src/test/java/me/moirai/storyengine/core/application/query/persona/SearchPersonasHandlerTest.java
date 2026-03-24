package me.moirai.storyengine.core.application.query.persona;

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
import me.moirai.storyengine.core.port.inbound.persona.PersonaSummary;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonas;
import me.moirai.storyengine.core.port.outbound.persona.PersonaSearchReader;

@ExtendWith(MockitoExtension.class)
public class SearchPersonasHandlerTest {

    @Mock
    private PersonaSearchReader reader;

    @InjectMocks
    private SearchPersonasHandler handler;

    @Test
    public void searchPersonas_whenValidQuery_thenReturnResult() {

        // Given
        var query = new SearchPersonas(
                "name",
                SearchView.MY_STUFF,
                null,
                null,
                1,
                2,
                1L);

        var expectedResult = PaginatedResult.<PersonaSummary>of(List.of(), 0L, 1, 2);

        when(reader.search(any(SearchPersonas.class))).thenReturn(expectedResult);

        // When
        var result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.page()).isEqualTo(expectedResult.page());
        assertThat(result.items()).isEqualTo(expectedResult.items());
    }
}
