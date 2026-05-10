package me.moirai.storyengine.core.port.outbound.adventure;

import java.util.List;
import java.util.UUID;

import me.moirai.storyengine.core.port.outbound.VectorSearchPortBase;

public interface LorebookVectorSearchPort extends VectorSearchPortBase {

    void upsert(UUID adventureId, UUID entryId, float[] vector);

    void delete(UUID entryId);

    List<UUID> search(UUID adventureId, float[] queryVector, int topK);
    void deleteAllByAdventureId(UUID adventureId);
}
