package me.moirai.storyengine.core.application.command.adventure;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.chronicle.ChronicleSegmentRepository;
import me.moirai.storyengine.core.port.outbound.message.MessageRepository;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import me.moirai.storyengine.core.port.outbound.vectorsearch.ChronicleVectorSearchPort;
import me.moirai.storyengine.core.port.outbound.vectorsearch.LorebookVectorSearchPort;

@ExtendWith(MockitoExtension.class)
public class DeleteAdventureHandlerTest {

    @Mock
    private AdventureRepository repository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChronicleSegmentRepository chronicleSegmentRepository;

    @Mock
    private LorebookVectorSearchPort lorebookVectorSearchPort;

    @Mock
    private ChronicleVectorSearchPort chronicleVectorSearchPort;

    @Mock
    private StoragePort storagePort;

    @InjectMocks
    private DeleteAdventureHandler handler;

    private Adventure adventureWithId() {
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);
        return adventure;
    }

    @Test
    public void errorWhenIdIsNull() {

        // given
        DeleteAdventure command = new DeleteAdventure(null);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void deleteAdventure_whenAdventureNotFound_thenThrowException() {

        // given
        DeleteAdventure command = new DeleteAdventure(AdventureFixture.PUBLIC_ID);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> handler.handle(command));
    }

    @Test
    public void deleteAdventure_whenFound_thenDeletesAllRelatedData() {

        // given
        var adventure = adventureWithId();
        var command = new DeleteAdventure(AdventureFixture.PUBLIC_ID);

        when(repository.findByPublicId(AdventureFixture.PUBLIC_ID)).thenReturn(Optional.of(adventure));

        // when
        handler.handle(command);

        // then
        verify(messageRepository).deleteAllByAdventurePublicId(AdventureFixture.PUBLIC_ID);
        verify(chronicleSegmentRepository).deleteAllByAdventurePublicId(AdventureFixture.PUBLIC_ID);
        verify(lorebookVectorSearchPort).deleteAllByAdventureId(AdventureFixture.PUBLIC_ID);
        verify(chronicleVectorSearchPort).deleteAllByAdventureId(AdventureFixture.PUBLIC_ID);
        verify(repository).deleteByPublicId(AdventureFixture.PUBLIC_ID);
    }

    @Test
    public void shouldDeleteImageWhenEntityHasImageKey() {

        // given
        var adventure = adventureWithId();
        ReflectionTestUtils.setField(adventure, "imageKey", "adventures/test/image.png");
        var command = new DeleteAdventure(AdventureFixture.PUBLIC_ID);

        when(repository.findByPublicId(AdventureFixture.PUBLIC_ID)).thenReturn(Optional.of(adventure));

        // when
        handler.handle(command);

        // then
        verify(storagePort).delete("adventures/test/image.png");
    }

    @Test
    public void shouldNotDeleteImageWhenEntityHasNoImageKey() {

        // given
        var adventure = adventureWithId();
        var command = new DeleteAdventure(AdventureFixture.PUBLIC_ID);

        when(repository.findByPublicId(AdventureFixture.PUBLIC_ID)).thenReturn(Optional.of(adventure));

        // when
        handler.handle(command);

        // then
        verify(storagePort, never()).delete(any());
    }
}
