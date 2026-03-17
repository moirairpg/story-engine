package me.moirai.storyengine.core.application.query.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntries;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntriesResult;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@ExtendWith(MockitoExtension.class)
public class SearchAdventureLorebookEntriesHandlerTest {

    @Mock
    private AdventureRepository adventureRepository;

    @InjectMocks
    private SearchAdventureLorebookEntriesHandler handler;

    @Test
    public void searchEntries_whenAdventureNotFound_thenThrowException() {

        // Given
        SearchAdventureLorebookEntries query = new SearchAdventureLorebookEntries(
                AdventureFixture.PUBLIC_ID,
                null,
                1,
                2,
                "name",
                "ASC",
                "1234");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> handler.execute(query))
                .isInstanceOf(AssetNotFoundException.class)
                .hasMessage("The adventure where the entries are being search doesn't exist");
    }

    @Test
    public void searchEntries_whenNoPermissionToView_thenThrowException() {

        // Given
        SearchAdventureLorebookEntries query = new SearchAdventureLorebookEntries(
                AdventureFixture.PUBLIC_ID,
                null,
                1,
                2,
                "name",
                "ASC",
                "1234");

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));

        // Then
        assertThatThrownBy(() -> handler.execute(query))
                .isInstanceOf(AssetAccessDeniedException.class)
                .hasMessage("User does not have permission to view this adventure");
    }

    @Test
    public void searchEntries_whenValidRequest_thenReturnEntries() {

        // Given
        SearchAdventureLorebookEntries query = new SearchAdventureLorebookEntries(
                AdventureFixture.PUBLIC_ID,
                null,
                1,
                2,
                "name",
                "ASC",
                "1234");

        SearchAdventureLorebookEntriesResult expectedResult = new SearchAdventureLorebookEntriesResult(
                1, 2, 0L, 0, List.of());

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure().build();

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(adventureRepository.searchLorebookEntries(any(SearchAdventureLorebookEntries.class)))
                .thenReturn(expectedResult);

        // When
        SearchAdventureLorebookEntriesResult result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).isEqualTo(expectedResult.items());
        assertThat(result.page()).isEqualTo(expectedResult.page());
    }
}
