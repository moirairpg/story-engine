package me.moirai.storyengine.core.port.outbound.vectorsearch;

import java.util.List;
import java.util.UUID;

public interface ChronicleVectorSearchPort {
    void upsert(UUID adventureId, UUID segmentId, float[] vector);
    List<UUID> search(UUID adventureId, float[] queryVector, int topK);
    void deleteAllByAdventureId(UUID adventureId);
}
