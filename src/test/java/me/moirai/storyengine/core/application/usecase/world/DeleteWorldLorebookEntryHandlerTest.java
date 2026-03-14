package me.moirai.storyengine.core.application.usecase.world;

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

import me.moirai.storyengine.core.port.inbound.world.DeleteWorldLorebookEntry;
import me.moirai.storyengine.core.domain.world.WorldService;

@ExtendWith(MockitoExtension.class)
public class DeleteWorldLorebookEntryHandlerTest {

    @Mock
    private WorldService domainService;

    @InjectMocks
    private DeleteWorldLorebookEntryHandler handler;

    @Test
    public void errorWhenEntryIdIsNull() {

        // Given
        DeleteWorldLorebookEntry command = DeleteWorldLorebookEntry.builder().build();

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void errorWhenWorldIdIsNull() {

        // Given
        DeleteWorldLorebookEntry command = DeleteWorldLorebookEntry.builder()
                .lorebookEntryId("DUMMY")
                .build();

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void deleteWorld() {

        // Given
        String id = "WRDID";
        String worldId = "WRLDID";
        String requesterId = "4234324";

        DeleteWorldLorebookEntry command = DeleteWorldLorebookEntry.builder()
                .lorebookEntryId(id)
                .worldId(worldId)
                .requesterId(requesterId)
                .build();

        doNothing().when(domainService).deleteLorebookEntry(any(DeleteWorldLorebookEntry.class));

        // When
        handler.handle(command);

        // Then
        verify(domainService, times(1)).deleteLorebookEntry(any());
    }
}
