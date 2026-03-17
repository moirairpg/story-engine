package me.moirai.storyengine.core.application.usecase.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        CreateWorldLorebookEntry command = new CreateWorldLorebookEntry(
                null,
                "Volin Habar",
                "[Vv]olin [Hh]abar|[Vv]oha",
                "Volin Habar is a warrior that fights with a sword.",
                null);

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void errorWhenNameIsNull() {

        // Given
        CreateWorldLorebookEntry command = new CreateWorldLorebookEntry(
                WorldFixture.PUBLIC_ID,
                null,
                "[Vv]olin [Hh]abar|[Vv]oha",
                "Volin Habar is a warrior that fights with a sword.",
                null);

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void errorWhenDescriptionIsNull() {

        // Given
        CreateWorldLorebookEntry command = new CreateWorldLorebookEntry(
                WorldFixture.PUBLIC_ID,
                "Volin Habar",
                "[Vv]olin [Hh]abar|[Vv]oha",
                null,
                null);

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void createWorldLorebookEntry() {

        // Given
        String requesterId = "OWNER123";
        CreateWorldLorebookEntry command = new CreateWorldLorebookEntry(
                WorldFixture.PUBLIC_ID,
                "Volin Habar",
                "[Vv]olin [Hh]abar|[Vv]oha",
                "Volin Habar is a warrior that fights with a sword.",
                requesterId);

        World world = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(world));
        when(repository.save(any())).thenReturn(world);

        // When
        WorldLorebookEntryDetails result = handler.handle(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(command.name());
    }
}
