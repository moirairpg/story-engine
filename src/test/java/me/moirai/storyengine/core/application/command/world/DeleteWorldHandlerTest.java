package me.moirai.storyengine.core.application.command.world;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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

    private static final String IMAGE_KEY = "worlds/test/image.png";

    @Mock
    private WorldRepository repository;

    @Mock
    private StoragePort storagePort;

    @InjectMocks
    private DeleteWorldHandler handler;

    @Test
    public void shouldThrowExceptionWhenIdIsNull() {

        // given
        var command = new DeleteWorld(null);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldDeleteTheWorldAndItsImageWhenTheWorldHasAnImage() {

        // given
        var world = WorldFixture.publicWorldWithId();
        ReflectionTestUtils.setField(world, "imageKey", IMAGE_KEY);

        var command = new DeleteWorld(WorldFixture.PUBLIC_ID);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(world));

        // when
        handler.handle(command);

        // then
        verify(repository).deleteByPublicId(WorldFixture.PUBLIC_ID);
        verify(storagePort).delete(IMAGE_KEY);
    }

    @Test
    public void shouldNotCallStorageWhenTheWorldHasNoImage() {

        // given
        var world = WorldFixture.publicWorldWithId();
        var command = new DeleteWorld(WorldFixture.PUBLIC_ID);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(world));

        // when
        handler.handle(command);

        // then
        verify(repository).deleteByPublicId(WorldFixture.PUBLIC_ID);
        verify(storagePort, never()).delete(any());
    }

    @Test
    public void shouldPropagateTheFailureWhenTheImageDeleteFails() {

        // given
        var world = WorldFixture.publicWorldWithId();
        ReflectionTestUtils.setField(world, "imageKey", IMAGE_KEY);

        var command = new DeleteWorld(WorldFixture.PUBLIC_ID);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(world));
        doThrow(new RuntimeException("storage down")).when(storagePort).delete(IMAGE_KEY);

        // then
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> handler.handle(command))
                .withMessage("storage down");
    }

    @Test
    public void shouldThrowExceptionWhenWorldNotFound() {

        // given
        var command = new DeleteWorld(WorldFixture.PUBLIC_ID);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> handler.handle(command));

        verify(repository, never()).deleteByPublicId(any());
        verify(storagePort, never()).delete(any());
    }
}
