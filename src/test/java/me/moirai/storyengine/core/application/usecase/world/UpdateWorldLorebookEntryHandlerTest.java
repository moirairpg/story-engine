package me.moirai.storyengine.core.application.usecase.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.port.inbound.world.UpdateWorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorldLorebookEntryResult;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntry;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntryFixture;
import me.moirai.storyengine.core.domain.world.WorldService;

@ExtendWith(MockitoExtension.class)
public class UpdateWorldLorebookEntryHandlerTest {

    @Mock
    private WorldService service;

    @InjectMocks
    private UpdateWorldLorebookEntryHandler handler;

    @Test
    public void updateWorld() {

        // Given
        String id = "WRDID";

        UpdateWorldLorebookEntry command = UpdateWorldLorebookEntry.builder()
                .id(id)
                .name("MoirAI")
                .regex("MoirAI")
                .description("This is an RPG world")
                .build();

        WorldLorebookEntry expectedUpdatedEntry = WorldLorebookEntryFixture.sampleLorebookEntry().build();

        when(service.updateLorebookEntry(any(UpdateWorldLorebookEntry.class)))
                .thenReturn(expectedUpdatedEntry);

        // When
        UpdateWorldLorebookEntryResult result = handler.handle(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLastUpdatedDateTime()).isEqualTo(expectedUpdatedEntry.getLastUpdateDate());
    }
}
