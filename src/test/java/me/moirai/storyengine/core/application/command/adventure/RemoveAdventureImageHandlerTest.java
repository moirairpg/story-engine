package me.moirai.storyengine.core.application.command.adventure;

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
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.port.inbound.adventure.RemoveAdventureImage;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;

@ExtendWith(MockitoExtension.class)
public class RemoveAdventureImageHandlerTest {

    @Mock
    private AdventureRepository repository;

    @Mock
    private StoragePort storagePort;

    @InjectMocks
    private RemoveAdventureImageHandler handler;

    private Adventure adventureWithId() {
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);
        return adventure;
    }

    @Test
    public void shouldDeleteImageWhenImageKeyIsPresent() {

        // given
        var adventure = adventureWithId();
        ReflectionTestUtils.setField(adventure, "imageKey", "adventures/test/image.png");
        var command = new RemoveAdventureImage(AdventureFixture.PUBLIC_ID);

        when(repository.findByPublicId(AdventureFixture.PUBLIC_ID)).thenReturn(Optional.of(adventure));
        when(repository.save(any())).thenReturn(adventure);

        // when
        handler.handle(command);

        // then
        verify(storagePort).delete("adventures/test/image.png");
        verify(repository).save(adventure);
    }

    @Test
    public void shouldDoNothingWhenImageKeyIsNull() {

        // given
        var adventure = adventureWithId();
        var command = new RemoveAdventureImage(AdventureFixture.PUBLIC_ID);

        when(repository.findByPublicId(AdventureFixture.PUBLIC_ID)).thenReturn(Optional.of(adventure));

        // when
        handler.handle(command);

        // then
        verify(storagePort, never()).delete(any());
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldThrowWhenAdventureIsNotFound() {

        // given
        var command = new RemoveAdventureImage(AdventureFixture.PUBLIC_ID);

        when(repository.findByPublicId(AdventureFixture.PUBLIC_ID)).thenReturn(Optional.empty());

        // then
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> handler.handle(command));
    }
}
