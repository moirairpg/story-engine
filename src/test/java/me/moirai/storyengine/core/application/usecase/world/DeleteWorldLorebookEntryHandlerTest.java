package me.moirai.storyengine.core.application.usecase.world;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
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
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.world.DeleteWorldLorebookEntry;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@ExtendWith(MockitoExtension.class)
public class DeleteWorldLorebookEntryHandlerTest {

    @Mock
    private WorldRepository repository;

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

        World baseWorld = WorldFixture.publicWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        World world = spy(baseWorld);
        doNothing().when(world).removeLorebookEntry(anyString());

        when(repository.findById(anyString())).thenReturn(Optional.of(world));
        when(repository.save(any())).thenReturn(world);

        // When
        handler.handle(command);

        // Then
        verify(repository, times(1)).save(any());
    }
}
