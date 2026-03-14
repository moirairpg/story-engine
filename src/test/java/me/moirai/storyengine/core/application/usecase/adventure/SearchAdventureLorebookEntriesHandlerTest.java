package me.moirai.storyengine.core.application.usecase.adventure;

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
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntries;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntriesResult;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureLorebookEntryRepository;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;

@ExtendWith(MockitoExtension.class)
public class SearchAdventureLorebookEntriesHandlerTest {

    @Mock
    private AdventureRepository adventureRepository;

    @Mock
    private AdventureLorebookEntryRepository repository;

    @InjectMocks
    private SearchAdventureLorebookEntriesHandler handler;

    @Test
    public void searchEntries_whenAdventureNotFound_thenThrowException() {

        // Given
        SearchAdventureLorebookEntries query = SearchAdventureLorebookEntries.builder()
                .direction("ASC")
                .page(1)
                .size(2)
                .sortingField("name")
                .adventureId("1234")
                .requesterId("1234")
                .build();

        when(adventureRepository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> handler.execute(query))
                .isInstanceOf(AssetNotFoundException.class)
                .hasMessage("The adventure where the entries are being search doesn't exist");
    }

    @Test
    public void searchEntries_whenNoPermissionToView_thenThrowException() {

        // Given
        SearchAdventureLorebookEntries query = SearchAdventureLorebookEntries.builder()
                .direction("ASC")
                .page(1)
                .size(2)
                .sortingField("name")
                .adventureId("1234")
                .requesterId("1234")
                .build();

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();

        when(adventureRepository.findById(anyString())).thenReturn(Optional.of(adventure));

        // Then
        assertThatThrownBy(() -> handler.execute(query))
                .isInstanceOf(AssetAccessDeniedException.class)
                .hasMessage("User does not have permission to view this adventure");
    }

    @Test
    public void searchEntries_whenValidRequest_thenReturnEntries() {

        // Given
        SearchAdventureLorebookEntries query = SearchAdventureLorebookEntries.builder()
                .direction("ASC")
                .page(1)
                .size(2)
                .sortingField("name")
                .adventureId("1234")
                .requesterId("1234")
                .build();

        SearchAdventureLorebookEntriesResult expectedResult = SearchAdventureLorebookEntriesResult.builder()
                .page(1)
                .items(2)
                .build();

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure().build();

        when(adventureRepository.findById(anyString())).thenReturn(Optional.of(adventure));
        when(repository.search(any(SearchAdventureLorebookEntries.class)))
                .thenReturn(expectedResult);

        // When
        SearchAdventureLorebookEntriesResult result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEqualTo(expectedResult.getItems());
        assertThat(result.getPage()).isEqualTo(expectedResult.getPage());
    }
}
