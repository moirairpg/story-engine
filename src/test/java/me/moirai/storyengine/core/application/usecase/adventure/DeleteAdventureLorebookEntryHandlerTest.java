package me.moirai.storyengine.core.application.usecase.adventure;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventureLorebookEntry;
import me.moirai.storyengine.core.domain.adventure.AdventureService;

@ExtendWith(MockitoExtension.class)
public class DeleteAdventureLorebookEntryHandlerTest {

    @Mock
    private AdventureService domainService;

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

        doNothing().when(domainService).deleteLorebookEntry(any(DeleteAdventureLorebookEntry.class));

        // When
        handler.handle(command);

        // Then
        verify(domainService, times(1)).deleteLorebookEntry(any());
    }
}
