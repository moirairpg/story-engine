package me.moirai.storyengine.core.application.command.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import me.moirai.storyengine.core.port.inbound.world.UploadWorldImage;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@ExtendWith(MockitoExtension.class)
public class UploadWorldImageHandlerTest {

    @Mock
    private WorldRepository repository;

    @Mock
    private StoragePort storagePort;

    @InjectMocks
    private UploadWorldImageHandler handler;

    @Test
    public void shouldUploadImageWhenWorldExistsAndFileIsValid() {

        // given
        var world = WorldFixture.publicWorldWithId();
        var command = new UploadWorldImage(WorldFixture.PUBLIC_ID, new byte[]{1, 2, 3}, "image/jpeg", "jpg");

        when(repository.findByPublicId(WorldFixture.PUBLIC_ID)).thenReturn(Optional.of(world));
        when(storagePort.resolveUrl(anyString())).thenReturn("http://localhost:9000/moirai/worlds/test/image.jpg");
        when(repository.save(any())).thenReturn(world);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.imageUrl()).isNotNull();
        verify(storagePort).upload(anyString(), eq(command.imageBytes()), eq("image/jpeg"));
    }

    @Test
    public void shouldDeletePreviousImageWhenWorldAlreadyHasImageKey() {

        // given
        var world = WorldFixture.publicWorldWithId();
        ReflectionTestUtils.setField(world, "imageKey", "worlds/old/image.jpg");
        var command = new UploadWorldImage(WorldFixture.PUBLIC_ID, new byte[]{1, 2, 3}, "image/jpeg", "jpg");

        when(repository.findByPublicId(WorldFixture.PUBLIC_ID)).thenReturn(Optional.of(world));
        when(storagePort.resolveUrl(anyString())).thenReturn("http://localhost:9000/moirai/worlds/test/image.jpg");
        when(repository.save(any())).thenReturn(world);

        // when
        handler.handle(command);

        // then
        verify(storagePort).delete("worlds/old/image.jpg");
        verify(storagePort).upload(anyString(), any(), any());
    }

    @Test
    public void shouldThrowWhenWorldIsNotFound() {

        // given
        var command = new UploadWorldImage(WorldFixture.PUBLIC_ID, new byte[]{1, 2, 3}, "image/jpeg", "jpg");

        when(repository.findByPublicId(WorldFixture.PUBLIC_ID)).thenReturn(Optional.empty());

        // then
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> handler.handle(command));

        verify(storagePort, never()).upload(any(), any(), any());
    }
}
