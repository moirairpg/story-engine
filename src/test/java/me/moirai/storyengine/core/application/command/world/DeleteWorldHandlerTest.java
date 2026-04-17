package me.moirai.storyengine.core.application.command.world;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.world.DeleteWorld;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@ExtendWith(MockitoExtension.class)
public class DeleteWorldHandlerTest {

    @Mock
    private WorldRepository repository;

    @Mock
    private StoragePort storagePort;

    @InjectMocks
    private DeleteWorldHandler handler;

    @Test
    public void errorWhenIdIsNull() {

        // given
        var config = new DeleteWorld(null);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(config));
    }

    @Test
    public void deleteWorld() {

        // given
        var world = WorldFixture.publicWorld().build();

        var command = new DeleteWorld(WorldFixture.PUBLIC_ID);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(world));
        doNothing().when(repository).deleteByPublicId(any(UUID.class));

        // when
        handler.handle(command);

        // then
        verify(repository, times(1)).deleteByPublicId(any(UUID.class));
    }

    @Test
    public void updateWorld_whenWorldNotFound_thenExceptionIsThrown() {

        // given
        var command = new DeleteWorld(WorldFixture.PUBLIC_ID);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void shouldDeleteImageWhenEntityHasImageKey() {

        // given
        var world = WorldFixture.publicWorld().build();
        ReflectionTestUtils.setField(world, "imageKey", "worlds/test/image.png");
        var command = new DeleteWorld(WorldFixture.PUBLIC_ID);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(world));
        doNothing().when(repository).deleteByPublicId(any(UUID.class));

        // when
        handler.handle(command);

        // then
        verify(storagePort).delete("worlds/test/image.png");
    }

    @Test
    public void shouldNotDeleteImageWhenEntityHasNoImageKey() {

        // given
        var world = WorldFixture.publicWorld().build();
        var command = new DeleteWorld(WorldFixture.PUBLIC_ID);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(world));
        doNothing().when(repository).deleteByPublicId(any(UUID.class));

        // when
        handler.handle(command);

        // then
        verify(storagePort, never()).delete(any());
    }
}
