package me.moirai.storyengine.core.application.usecase.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntry;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntryFixture;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntryRepository;
import me.moirai.storyengine.core.domain.world.WorldRepository;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorldLorebookEntryResult;

@ExtendWith(MockitoExtension.class)
public class UpdateWorldLorebookEntryHandlerTest {

    @Mock
    private WorldLorebookEntryRepository lorebookEntryRepository;

    @Mock
    private WorldRepository repository;

    @InjectMocks
    private UpdateWorldLorebookEntryHandler handler;

    @Test
    public void updateWorld() {

        // Given
        String id = "WRDID";
        String requesterId = "OWNER123";

        UpdateWorldLorebookEntry command = UpdateWorldLorebookEntry.builder()
                .id(id)
                .name("MoirAI")
                .regex("MoirAI")
                .description("This is an RPG world")
                .worldId("WRLDID")
                .requesterId(requesterId)
                .build();

        World world = WorldFixture.publicWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        WorldLorebookEntry expectedUpdatedEntry = WorldLorebookEntryFixture.sampleLorebookEntry().build();

        when(repository.findById(anyString())).thenReturn(Optional.of(world));
        when(lorebookEntryRepository.findById(anyString())).thenReturn(Optional.of(expectedUpdatedEntry));
        when(lorebookEntryRepository.save(any())).thenReturn(expectedUpdatedEntry);

        // When
        UpdateWorldLorebookEntryResult result = handler.handle(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLastUpdatedDateTime()).isEqualTo(expectedUpdatedEntry.getLastUpdateDate());
    }
}
