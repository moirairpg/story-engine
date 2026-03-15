package me.moirai.storyengine.core.application.usecase.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.application.usecase.world.request.CreateWorldLorebookEntryFixture;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.world.CreateWorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@ExtendWith(MockitoExtension.class)
public class CreateWorldLorebookEntryHandlerTest {

    @Mock
    private WorldRepository repository;

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
        String requesterId = "OWNER123";
        CreateWorldLorebookEntry command = CreateWorldLorebookEntryFixture.sampleLorebookEntry()
                .requesterId(requesterId)
                .build();

        World world = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(world));
        when(repository.save(any())).thenReturn(world);

        // When
        WorldLorebookEntryDetails result = handler.handle(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(command.getName());
    }
}
