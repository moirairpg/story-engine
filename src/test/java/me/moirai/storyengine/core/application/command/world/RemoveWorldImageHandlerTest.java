package me.moirai.storyengine.core.application.command.world;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.world.RemoveWorldImage;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@ExtendWith(MockitoExtension.class)
public class RemoveWorldImageHandlerTest {

    @Mock
    private WorldRepository repository;

    @Mock
    private StoragePort storagePort;

    @InjectMocks
    private RemoveWorldImageHandler handler;

    @Test
    public void shouldDeleteImageWhenImageKeyIsPresent() {

        // given
        var world = WorldFixture.publicWorldWithId();
        ReflectionTestUtils.setField(world, "imageKey", "worlds/test/image.png");
        var command = new RemoveWorldImage(WorldFixture.PUBLIC_ID);

        when(repository.findByPublicId(WorldFixture.PUBLIC_ID)).thenReturn(Optional.of(world));
        when(repository.save(any())).thenReturn(world);

        // when
        handler.handle(command);

        // then
        verify(storagePort).delete("worlds/test/image.png");
        verify(repository).save(world);
    }

    @Test
    public void shouldDoNothingWhenImageKeyIsNull() {

        // given
        var world = WorldFixture.publicWorldWithId();
        var command = new RemoveWorldImage(WorldFixture.PUBLIC_ID);

        when(repository.findByPublicId(WorldFixture.PUBLIC_ID)).thenReturn(Optional.of(world));

        // when
        handler.handle(command);

        // then
        verify(storagePort, never()).delete(any());
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldThrowWhenWorldIsNotFound() {

        // given
        var command = new RemoveWorldImage(WorldFixture.PUBLIC_ID);

        when(repository.findByPublicId(WorldFixture.PUBLIC_ID)).thenReturn(Optional.empty());

        // then
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> handler.handle(command));
    }
}
