package me.moirai.storyengine.core.port.outbound.vectorsearch;

import java.util.List;
import java.util.UUID;

public interface LorebookVectorSearchPort {

    void upsert(UUID adventureId, UUID entryId, float[] vector);

    void delete(UUID entryId);

    List<UUID> search(UUID adventureId, float[] queryVector, int topK);
    void deleteAllByAdventureId(UUID adventureId);
}
