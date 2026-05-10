package me.moirai.storyengine.core.port.outbound.character;

import java.util.List;
import java.util.UUID;

import me.moirai.storyengine.core.port.outbound.VectorSearchPortBase;

public interface PlayerCharacterVectorSearchPort extends VectorSearchPortBase{

    void upsert(UUID characterId, float[] vector);

    void delete(UUID characterId);

    List<UUID> search(List<UUID> candidateIds, float[] queryVector, int topK);
}