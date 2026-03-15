package me.moirai.storyengine.core.application.usecase.world;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.port.inbound.world.DeleteWorld;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;

@ExtendWith(MockitoExtension.class)
public class DeleteWorldHandlerTest {

    @Mock
    private WorldRepository repository;

    @InjectMocks
    private DeleteWorldHandler handler;

    @Test
    public void errorWhenIdIsNull() {

        // Given
        String id = null;
        String requesterId = "84REAC";
        DeleteWorld config = DeleteWorld.build(id, requesterId);

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(config));
    }

    @Test
    public void deleteWorld() {

        // Given
        String requesterId = "84REAC";
        String id = "WRDID";
        World world = WorldFixture.publicWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .usersAllowedToRead(Collections.emptySet())
                        .build())
                .build();

        DeleteWorld command = DeleteWorld.build(id, requesterId);

        when(repository.findByPublicId(anyString())).thenReturn(Optional.of(world));
        doNothing().when(repository).deleteByPublicId(anyString());

        // When
        handler.handle(command);

        // Then
        verify(repository, times(1)).deleteByPublicId(anyString());
    }

    @Test
    public void updateWorld_whenWorldNotFound_thenExceptionIsThrown() {

        // Given
        String id = WorldFixture.PUBLIC_ID;
        String requesterId = "84REAC";
        DeleteWorld command = DeleteWorld.build(id, requesterId);

        when(repository.findByPublicId(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(AssetNotFoundException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void deleteWorld_whenAccessDenied_thenThrowException() {

        // Given
        String id = WorldFixture.PUBLIC_ID;
        String requesterId = "RQSTRID";
        DeleteWorld command = DeleteWorld.build(id, requesterId);

        World world = WorldFixture.privateWorld()
                .build();

        when(repository.findByPublicId(anyString())).thenReturn(Optional.of(world));

        // Then
        assertThatExceptionOfType(AssetAccessDeniedException.class)
                .isThrownBy(() -> handler.handle(command));
    }
}
