package me.moirai.storyengine.core.application.usecase.world;

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
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntry;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntryFixture;
import me.moirai.storyengine.core.port.inbound.world.GetWorldLorebookEntryById;
import me.moirai.storyengine.core.port.inbound.world.GetWorldLorebookEntryResult;
import me.moirai.storyengine.core.port.outbound.world.WorldLorebookEntryRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@ExtendWith(MockitoExtension.class)
public class GetWorldLorebookEntryByIdHandlerTest {

    @Mock
    private WorldLorebookEntryRepository lorebookEntryRepository;

    @Mock
    private WorldRepository repository;

    @InjectMocks
    private GetWorldLorebookEntryByIdHandler handler;

    @Test
    public void errorWhenQueryIsNull() {

        // Given
        GetWorldLorebookEntryById query = null;

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void errorWhenEntryIdIsNull() {

        // Given
        GetWorldLorebookEntryById query = GetWorldLorebookEntryById.builder().build();

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void errorWhenWorldIdIsNull() {

        // Given
        GetWorldLorebookEntryById query = GetWorldLorebookEntryById.builder()
                .entryId("ENTRID")
                .build();

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void getWorldLorebookEntryById() {

        // Given
        String id = "HAUDHUAHD";
        String worldId = "WRLDID";
        String requesterId = "4314324";

        WorldLorebookEntry entry = WorldLorebookEntryFixture.sampleLorebookEntry().id(id).build();

        World world = WorldFixture.publicWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        GetWorldLorebookEntryById query = GetWorldLorebookEntryById.builder()
                .entryId(id)
                .worldId(worldId)
                .requesterId(requesterId)
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(world));
        when(lorebookEntryRepository.findById(anyString())).thenReturn(Optional.of(entry));

        // When
        GetWorldLorebookEntryResult result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
    }
}
