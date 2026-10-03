package me.moirai.storyengine.core.application.event.adventure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import me.moirai.storyengine.core.domain.adventure.AdventureDeletedEvent;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntryRemovedEvent;
import me.moirai.storyengine.core.port.outbound.adventure.ChronicleVectorSearchPort;
import me.moirai.storyengine.core.port.outbound.adventure.LorebookVectorSearchPort;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;

@Component
public class AdventureDeletedCleanupListener {

    private static final Logger LOG = LoggerFactory.getLogger(AdventureDeletedCleanupListener.class);

    private static final String IMAGE_CLEANUP_FAILED = "Orphaned image after deleting adventure {}: key {}";
    private static final String LOREBOOK_CLEANUP_FAILED = "Orphaned lorebook vectors after deleting adventure {}";
    private static final String CHRONICLE_CLEANUP_FAILED = "Orphaned chronicle vectors after deleting adventure {}";

    private final StoragePort storagePort;
    private final LorebookVectorSearchPort lorebookVectorSearchPort;
    private final ChronicleVectorSearchPort chronicleVectorSearchPort;

    public AdventureDeletedCleanupListener(
            StoragePort storagePort,
            LorebookVectorSearchPort lorebookVectorSearchPort,
            ChronicleVectorSearchPort chronicleVectorSearchPort) {

        this.storagePort = storagePort;
        this.lorebookVectorSearchPort = lorebookVectorSearchPort;
        this.chronicleVectorSearchPort = chronicleVectorSearchPort;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAdventureDeleted(AdventureDeletedEvent event) {

        removeImage(event);
        removeLorebookVectors(event);
        removeChronicleVectors(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLorebookEntryRemoved(AdventureLorebookEntryRemovedEvent event) {

        lorebookVectorSearchPort.delete(event.getEntryId());
    }

    private void removeImage(AdventureDeletedEvent event) {

        if (event.getImageKey() == null) {
            return;
        }

        try {
            storagePort.delete(event.getImageKey());
        } catch (RuntimeException e) {
            LOG.error(IMAGE_CLEANUP_FAILED, event.getPublicId(), event.getImageKey(), e);
        }
    }

    private void removeLorebookVectors(AdventureDeletedEvent event) {

        try {
            lorebookVectorSearchPort.deleteAllByAdventureId(event.getPublicId());
        } catch (RuntimeException e) {
            LOG.error(LOREBOOK_CLEANUP_FAILED, event.getPublicId(), e);
        }
    }

    private void removeChronicleVectors(AdventureDeletedEvent event) {

        try {
            chronicleVectorSearchPort.deleteAllByAdventureId(event.getPublicId());
        } catch (RuntimeException e) {
            LOG.error(CHRONICLE_CLEANUP_FAILED, event.getPublicId(), e);
        }
    }
}
