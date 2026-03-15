package me.moirai.storyengine.core.application.usecase.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntries;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntriesResult;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@ExtendWith(MockitoExtension.class)
public class SearchWorldLorebookEntriesHandlerTest {

    @Mock
    private WorldRepository worldRepository;

    @InjectMocks
    private SearchWorldLorebookEntriesHandler handler;

    @Test
    public void searchEntries_whenWorldNotFound_thenThrowException() {

        // Given
        SearchWorldLorebookEntries query = SearchWorldLorebookEntries.builder()
                .direction("ASC")
                .page(1)
                .size(2)
                .sortingField("name")
                .worldId("1234")
                .requesterId("1234")
                .build();

        when(worldRepository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> handler.execute(query))
                .isInstanceOf(AssetNotFoundException.class)
                .hasMessage("The world where the entries are being search doesn't exist");
    }

    @Test
    public void searchEntries_whenNoPermissionToView_thenThrowException() {

        // Given
        SearchWorldLorebookEntries query = SearchWorldLorebookEntries.builder()
                .direction("ASC")
                .page(1)
                .size(2)
                .sortingField("name")
                .worldId("1234")
                .requesterId("1234")
                .build();

        World world = WorldFixture.privateWorld().build();

        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));

        // Then
        assertThatThrownBy(() -> handler.execute(query))
                .isInstanceOf(AssetAccessDeniedException.class)
                .hasMessage("User does not have permission to view this world");
    }

    @Test
    public void searchEntries_whenValidRequest_thenReturnEntries() {

        // Given
        SearchWorldLorebookEntries query = SearchWorldLorebookEntries.builder()
                .direction("ASC")
                .page(1)
                .size(2)
                .sortingField("name")
                .worldId("1234")
                .requesterId("1234")
                .build();

        SearchWorldLorebookEntriesResult expectedResult = SearchWorldLorebookEntriesResult.builder()
                .page(1)
                .items(2)
                .build();

        World world = WorldFixture.publicWorld().build();

        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));
        when(worldRepository.searchLorebookEntries(any(SearchWorldLorebookEntries.class)))
                .thenReturn(expectedResult);

        // When
        SearchWorldLorebookEntriesResult result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEqualTo(expectedResult.getItems());
        assertThat(result.getPage()).isEqualTo(expectedResult.getPage());
    }
}
