package me.moirai.storyengine.core.application.command.adventure;

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
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.port.inbound.adventure.UploadAdventureImage;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;

@ExtendWith(MockitoExtension.class)
public class UploadAdventureImageHandlerTest {

    @Mock
    private AdventureRepository repository;

    @Mock
    private StoragePort storagePort;

    @InjectMocks
    private UploadAdventureImageHandler handler;

    private Adventure adventureWithId() {
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);
        return adventure;
    }

    @Test
    public void shouldUploadImageWhenAdventureExistsAndFileIsValid() {

        // given
        var adventure = adventureWithId();
        var command = new UploadAdventureImage(AdventureFixture.PUBLIC_ID, new byte[]{1, 2, 3}, "image/jpeg", "jpg");

        when(repository.findByPublicId(AdventureFixture.PUBLIC_ID)).thenReturn(Optional.of(adventure));
        when(storagePort.resolveUrl(anyString())).thenReturn("http://localhost:9000/moirai/adventures/test/image.jpg");
        when(repository.save(any())).thenReturn(adventure);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.imageUrl()).isNotNull();
        verify(storagePort).upload(anyString(), eq(command.imageBytes()), eq("image/jpeg"));
    }

    @Test
    public void shouldDeletePreviousImageWhenAdventureAlreadyHasImageKey() {

        // given
        var adventure = adventureWithId();
        ReflectionTestUtils.setField(adventure, "imageKey", "adventures/old/image.jpg");
        var command = new UploadAdventureImage(AdventureFixture.PUBLIC_ID, new byte[]{1, 2, 3}, "image/jpeg", "jpg");

        when(repository.findByPublicId(AdventureFixture.PUBLIC_ID)).thenReturn(Optional.of(adventure));
        when(storagePort.resolveUrl(anyString())).thenReturn("http://localhost:9000/moirai/adventures/test/image.jpg");
        when(repository.save(any())).thenReturn(adventure);

        // when
        handler.handle(command);

        // then
        verify(storagePort).delete("adventures/old/image.jpg");
        verify(storagePort).upload(anyString(), any(), any());
    }

    @Test
    public void shouldThrowWhenAdventureIsNotFound() {

        // given
        var command = new UploadAdventureImage(AdventureFixture.PUBLIC_ID, new byte[]{1, 2, 3}, "image/jpeg", "jpg");

        when(repository.findByPublicId(AdventureFixture.PUBLIC_ID)).thenReturn(Optional.empty());

        // then
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> handler.handle(command));

        verify(storagePort, never()).upload(any(), any(), any());
    }
}
