package me.moirai.storyengine.core.application.usecase.adventure;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventureLorebookEntry;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureLorebookEntryRepository;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@ExtendWith(MockitoExtension.class)
public class DeleteAdventureLorebookEntryHandlerTest {

    @Mock
    private AdventureLorebookEntryRepository lorebookEntryRepository;

    @Mock
    private AdventureRepository repository;

    @InjectMocks
    private DeleteAdventureLorebookEntryHandler handler;

    @Test
    public void errorWhenEntryIdIsNull() {

        // Given
        DeleteAdventureLorebookEntry command = DeleteAdventureLorebookEntry.builder().build();

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void errorWhenAdventureIdIsNull() {

        // Given
        DeleteAdventureLorebookEntry command = DeleteAdventureLorebookEntry.builder()
                .lorebookEntryId("DUMMY")
                .build();

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void deleteAdventure() {

        // Given
        String id = "WRDID";
        String adventureId = "WRLDID";
        String requesterId = "4234324";

        DeleteAdventureLorebookEntry command = DeleteAdventureLorebookEntry.builder()
                .lorebookEntryId(id)
                .adventureId(adventureId)
                .requesterId(requesterId)
                .build();

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        AdventureLorebookEntry entry = AdventureLorebookEntryFixture.sampleLorebookEntry().build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));
        when(lorebookEntryRepository.findById(anyString())).thenReturn(Optional.of(entry));

        // When
        handler.handle(command);

        // Then
        verify(lorebookEntryRepository, times(1)).deleteById(anyString());
    }
}
