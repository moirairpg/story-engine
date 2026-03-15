package me.moirai.storyengine.core.application.usecase.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
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
import me.moirai.storyengine.core.port.inbound.world.UpdateWorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@ExtendWith(MockitoExtension.class)
public class UpdateWorldLorebookEntryHandlerTest {

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
                .worldId(WorldFixture.PUBLIC_ID)
                .requesterId(requesterId)
                .build();

        WorldLorebookEntry expectedUpdatedEntry = WorldLorebookEntryFixture.sampleLorebookEntry().build();

        World baseWorld = WorldFixture.publicWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        World world = spy(baseWorld);
        doReturn(expectedUpdatedEntry).when(world).updateLorebookEntry(anyString(), anyString(), anyString(), anyString());

        when(repository.findByPublicId(anyString())).thenReturn(Optional.of(world));
        when(repository.save(any())).thenReturn(world);

        // When
        WorldLorebookEntryDetails result = handler.handle(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLastUpdateDate()).isEqualTo(expectedUpdatedEntry.getLastUpdateDate());
    }
}
