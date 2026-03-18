package me.moirai.storyengine.core.application.command.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorld;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@ExtendWith(MockitoExtension.class)
public class UpdateWorldHandlerTest {

    @Mock
    private WorldRepository repository;

    @InjectMocks
    private UpdateWorldHandler handler;

    @Test
    public void updateWorld_whenFieldsAreProvided_thenUpdateWorld() {

        // Given
        var id = WorldFixture.PUBLIC_ID;
        var requesterId = "RQSTRID";
        var newName = "NEW NAME";
        var command = new UpdateWorld(
                id,
                "MoirAI",
                "This is an RPG world",
                "As you enter the city, people around you start looking at you.",
                Visibility.PUBLIC,
                requesterId,
                null,
                null,
                null,
                null);

        var expectedUpdatedWorld = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        var unchangedWorld = WorldFixture.privateWorld()
                .name(newName)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedWorld));
        when(repository.save(any(World.class))).thenReturn(expectedUpdatedWorld);

        // When
        var result = handler.handle(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.lastUpdateDate()).isEqualTo(expectedUpdatedWorld.getLastUpdateDate());
    }

    @Test
    public void updateWorld_whenValidData_thenWorldIsUpdated() {

        // Given
        var id = WorldFixture.PUBLIC_ID;
        var requesterId = "RQSTRID";
        var command = new UpdateWorld(
                id,
                "MoirAI",
                "This is an RPG world",
                "As you enter the city, people around you start looking at you.",
                Visibility.PUBLIC,
                requesterId,
                null,
                null,
                null,
                null);

        var unchangedWorld = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        var expectedUpdatedWorld = WorldFixture.privateWorld()
                .name("MoirAI")
                .description("This is an RPG world")
                .adventureStart("As you enter the city, people around you start looking at you.")
                .visibility(Visibility.PUBLIC)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedWorld));
        when(repository.save(any(World.class))).thenReturn(expectedUpdatedWorld);

        // When
        var result = handler.handle(command);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    public void updateWorld_whenEmptyUpdateFields_thenWorldIsNotChanged() {

        // Given
        var id = WorldFixture.PUBLIC_ID;
        var requesterId = "RQSTRID";
        var command = new UpdateWorld(
                id,
                null,
                null,
                null,
                null,
                requesterId,
                null,
                null,
                null,
                null);

        var unchangedWorld = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedWorld));
        when(repository.save(any(World.class))).thenReturn(unchangedWorld);

        // When
        var result = handler.handle(command);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    public void updateWorld_whenPublicToBeMadePrivate_thenWorldIsMadePrivate() {

        // Given
        var id = WorldFixture.PUBLIC_ID;
        var requesterId = "RQSTRID";
        var command = new UpdateWorld(
                id,
                null,
                null,
                null,
                Visibility.PRIVATE,
                requesterId,
                null,
                null,
                null,
                null);

        var unchangedWorld = WorldFixture.publicWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        var expectedWorld = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedWorld));
        when(repository.save(any(World.class))).thenReturn(expectedWorld);

        // When
        var result = handler.handle(command);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    public void updateWorld_whenInvalidVisibility_thenNothingIsChanged() {

        // Given
        var id = WorldFixture.PUBLIC_ID;
        var requesterId = "RQSTRID";
        var command = new UpdateWorld(
                id,
                null,
                null,
                null,
                null,
                requesterId,
                null,
                null,
                null,
                null);

        var unchangedWorld = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedWorld));
        when(repository.save(any(World.class))).thenReturn(unchangedWorld);

        // When
        var result = handler.handle(command);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    public void updateWorld_whenAccessDenied_thenExceptionIsThrown() {

        // Given
        var id = WorldFixture.PUBLIC_ID;
        var requesterId = "RQSTRID";
        var command = new UpdateWorld(
                id,
                "MoirAI",
                "This is an RPG world",
                "As you enter the city, people around you start looking at you.",
                Visibility.PUBLIC,
                requesterId,
                null,
                null,
                null,
                null);

        var unchangedWorld = WorldFixture.privateWorld()
                .name("NEW NAME")
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedWorld));

        // Then
        assertThatExceptionOfType(AssetAccessDeniedException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void updateWorld_whenIdIsNull_thenExceptionIsThrown() {

        // Given
        var command = new UpdateWorld(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void updateWorld_whenWorldNotFound_thenExceptionIsThrown() {

        // Given
        var command = new UpdateWorld(
                WorldFixture.PUBLIC_ID,
                "SomeNewName",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(AssetNotFoundException.class)
                .isThrownBy(() -> handler.handle(command));
    }
}
