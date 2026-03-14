package me.moirai.storyengine.core.application.usecase.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.application.usecase.world.request.CreateWorldLorebookEntry;
import me.moirai.storyengine.core.application.usecase.world.request.CreateWorldLorebookEntryFixture;
import me.moirai.storyengine.core.application.usecase.world.result.CreateWorldLorebookEntryResult;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntry;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntryFixture;
import me.moirai.storyengine.core.domain.world.WorldService;

@ExtendWith(MockitoExtension.class)
public class CreateWorldLorebookEntryHandlerTest {

    @Mock
    private WorldService domainService;

    @InjectMocks
    private CreateWorldLorebookEntryHandler handler;

    @Test
    public void errorWhenCommandIsNull() {

        // Given
        CreateWorldLorebookEntry command = null;

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void errorWhenWorldIdIsNull() {

        // Given
        CreateWorldLorebookEntry command = CreateWorldLorebookEntryFixture.sampleLorebookEntry()
                .worldId(null)
                .build();

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void errorWhenNameIsNull() {

        // Given
        CreateWorldLorebookEntry command = CreateWorldLorebookEntryFixture.sampleLorebookEntry()
                .name(null)
                .build();

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void errorWhenDescriptionIsNull() {

        // Given
        CreateWorldLorebookEntry command = CreateWorldLorebookEntryFixture.sampleLorebookEntry()
                .description(null)
                .build();

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void createWorldLorebookEntry() {

        // Given
        String id = "HAUDHUAHD";
        WorldLorebookEntry entry = WorldLorebookEntryFixture.sampleLorebookEntry().id(id).build();
        CreateWorldLorebookEntry command = CreateWorldLorebookEntryFixture.sampleLorebookEntry().build();

        when(domainService.createLorebookEntry(any(CreateWorldLorebookEntry.class)))
                .thenReturn(entry);

        // When
        CreateWorldLorebookEntryResult result = handler.handle(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
    }
}
