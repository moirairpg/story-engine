package me.moirai.storyengine.core.application.event.adventure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureDeletedEvent;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntryRemovedEvent;
import me.moirai.storyengine.core.port.outbound.adventure.ChronicleVectorSearchPort;
import me.moirai.storyengine.core.port.outbound.adventure.LorebookVectorSearchPort;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;

@ExtendWith(MockitoExtension.class)
public class AdventureDeletedCleanupListenerTest {

    private static final String IMAGE_KEY = "adventures/keep.png";

    @Mock
    private StoragePort storagePort;

    @Mock
    private LorebookVectorSearchPort lorebookVectorSearchPort;

    @Mock
    private ChronicleVectorSearchPort chronicleVectorSearchPort;

    @InjectMocks
    private AdventureDeletedCleanupListener listener;

    @Test
    void shouldRemoveTheImageAndBothVectorCollectionsWhenTheAdventureIsDeleted() {

        // given
        var adventure = adventureWithImage(IMAGE_KEY);

        // when
        listener.onAdventureDeleted(deletionEventFor(adventure));

        // then
        verify(storagePort).delete(IMAGE_KEY);
        verify(lorebookVectorSearchPort).deleteAllByAdventureId(AdventureFixture.PUBLIC_ID);
        verify(chronicleVectorSearchPort).deleteAllByAdventureId(AdventureFixture.PUBLIC_ID);
    }

    @Test
    void shouldSkipTheImageWhenTheAdventureHasNone() {

        // given
        var adventure = adventureWithImage(null);

        // when
        listener.onAdventureDeleted(deletionEventFor(adventure));

        // then
        verify(storagePort, never()).delete(any());
        verify(lorebookVectorSearchPort).deleteAllByAdventureId(AdventureFixture.PUBLIC_ID);
        verify(chronicleVectorSearchPort).deleteAllByAdventureId(AdventureFixture.PUBLIC_ID);
    }

    @Test
    void shouldStillRemoveBothVectorCollectionsWhenTheImageDeleteFails() {

        // given
        var adventure = adventureWithImage(IMAGE_KEY);

        doThrow(new RuntimeException("storage down")).when(storagePort).delete(any());

        // when
        listener.onAdventureDeleted(deletionEventFor(adventure));

        // then
        verify(lorebookVectorSearchPort).deleteAllByAdventureId(AdventureFixture.PUBLIC_ID);
        verify(chronicleVectorSearchPort).deleteAllByAdventureId(AdventureFixture.PUBLIC_ID);
    }

    @Test
    void shouldStillRemoveChronicleVectorsWhenTheLorebookVectorDeleteFails() {

        // given
        var adventure = adventureWithImage(IMAGE_KEY);

        doThrow(new RuntimeException("qdrant down")).when(lorebookVectorSearchPort).deleteAllByAdventureId(any());

        // when
        listener.onAdventureDeleted(deletionEventFor(adventure));

        // then
        verify(storagePort).delete(IMAGE_KEY);
        verify(chronicleVectorSearchPort).deleteAllByAdventureId(AdventureFixture.PUBLIC_ID);
    }

    @Test
    void shouldNotPropagateWhenEveryCleanupFails() {

        // given
        var adventure = adventureWithImage(IMAGE_KEY);

        doThrow(new RuntimeException("storage down")).when(storagePort).delete(any());
        doThrow(new RuntimeException("qdrant down")).when(lorebookVectorSearchPort).deleteAllByAdventureId(any());
        doThrow(new RuntimeException("qdrant down")).when(chronicleVectorSearchPort).deleteAllByAdventureId(any());

        // when
        listener.onAdventureDeleted(deletionEventFor(adventure));

        // then
        verify(storagePort).delete(IMAGE_KEY);
        verify(lorebookVectorSearchPort).deleteAllByAdventureId(AdventureFixture.PUBLIC_ID);
        verify(chronicleVectorSearchPort).deleteAllByAdventureId(AdventureFixture.PUBLIC_ID);
    }

    @Test
    void shouldRemoveTheEntryVectorWhenALorebookEntryIsRemoved() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        var entry = adventure.addLorebookEntry("Name", "Description");

        adventure.removeLorebookEntry(entry.getPublicId());

        var event = (AdventureLorebookEntryRemovedEvent) adventure.drainEvents().getFirst();

        // when
        listener.onLorebookEntryRemoved(event);

        // then
        verify(lorebookVectorSearchPort).delete(entry.getPublicId());
        verify(storagePort, never()).delete(any());
        verify(chronicleVectorSearchPort, never()).deleteAllByAdventureId(any());
    }

    private Adventure adventureWithImage(String imageKey) {

        var adventure = AdventureFixture.privateAdventureWithId();
        ReflectionTestUtils.setField(adventure, "imageKey", imageKey);

        return adventure;
    }

    private AdventureDeletedEvent deletionEventFor(Adventure adventure) {

        adventure.communicateAdventureDeleted();

        return (AdventureDeletedEvent) adventure.drainEvents().getFirst();
    }
}
