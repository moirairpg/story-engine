package me.moirai.storyengine.core.application.usecase.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntry;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntryFixture;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureLorebookEntryById;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureLorebookEntryRepository;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@ExtendWith(MockitoExtension.class)
public class GetAdventureLorebookEntryByIdHandlerTest {

    @Mock
    private AdventureLorebookEntryRepository lorebookEntryRepository;

    @Mock
    private AdventureRepository repository;

    @InjectMocks
    private GetAdventureLorebookEntryByIdHandler handler;

    @Test
    public void errorWhenQueryIsNull() {

        // Given
        GetAdventureLorebookEntryById query = null;

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void errorWhenEntryIdIsNull() {

        // Given
        GetAdventureLorebookEntryById query = GetAdventureLorebookEntryById.builder().build();

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void errorWhenAdventureIdIsNull() {

        // Given
        GetAdventureLorebookEntryById query = GetAdventureLorebookEntryById.builder()
                .entryId("ENTRID")
                .build();

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void getAdventureLorebookEntryById() {

        // Given
        String id = "HAUDHUAHD";
        String adventureId = "WRLDID";
        String requesterId = "4314324";

        AdventureLorebookEntry entry = AdventureLorebookEntryFixture.sampleLorebookEntry().id(id).build();

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        GetAdventureLorebookEntryById query = GetAdventureLorebookEntryById.builder()
                .entryId(id)
                .adventureId(adventureId)
                .requesterId(requesterId)
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));
        when(lorebookEntryRepository.findById(anyString())).thenReturn(Optional.of(entry));

        // When
        AdventureLorebookEntryDetails result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(entry.getId());
        assertThat(result.getName()).isEqualTo(entry.getName());
        assertThat(result.getRegex()).isEqualTo(entry.getRegex());
        assertThat(result.getDescription()).isEqualTo(entry.getDescription());
        assertThat(result.getPlayerId()).isEqualTo(entry.getPlayerId());
        assertThat(result.isPlayerCharacter()).isEqualTo(entry.isPlayerCharacter());
        assertThat(result.getCreationDate()).isNotNull();
        assertThat(result.getLastUpdateDate()).isNotNull();
    }
}
