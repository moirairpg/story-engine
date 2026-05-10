package me.moirai.storyengine.infrastructure.outbound.adapter.character;

import static io.qdrant.client.ConditionFactory.matchKeyword;
import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.grpc.StatusRuntimeException;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Common.Filter;
import io.qdrant.client.grpc.Points.DeletePoints;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.PointsIdsList;
import io.qdrant.client.grpc.Points.PointsSelector;
import io.qdrant.client.grpc.Points.SearchPoints;
import io.qdrant.client.grpc.Points.UpsertPoints;
import io.qdrant.client.grpc.Points.WithPayloadSelector;
import io.qdrant.client.grpc.PointsGrpc;
import io.qdrant.client.grpc.PointsGrpc.PointsBlockingStub;
import me.moirai.storyengine.common.exception.TechnicalException;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterVectorSearchPort;

@Component
public class PlayerCharacterVectorSearchAdapter implements PlayerCharacterVectorSearchPort {

    private final PointsBlockingStub qdrantClient;
    private final String collectionName;

    public PlayerCharacterVectorSearchAdapter(
            QdrantGrpcClient qdrantClient,
            @Value("${moirai.rag.player-character.collection-name}") String collectionName) {

        this.qdrantClient = PointsGrpc.newBlockingStub(qdrantClient.channel());
        this.collectionName = collectionName;
    }

    @Override
    public void upsert(UUID characterId, float[] vector) {

        var payload = Map.of("characterId", value(characterId.toString()));
        var point = PointStruct.newBuilder()
                .setId(id(characterId))
                .setVectors(vectors(toFloatList(vector)))
                .putAllPayload(payload)
                .build();

        var upsertRequest = UpsertPoints.newBuilder()
                .setCollectionName(collectionName)
                .addAllPoints(List.of(point))
                .build();

        try {
            qdrantClient.upsert(upsertRequest);
        } catch (StatusRuntimeException e) {
            Thread.currentThread().interrupt();
            throw new TechnicalException("Failed to upsert vector for character " + characterId);
        }
    }

    @Override
    public void delete(UUID characterId) {

        var deleteRequest = DeletePoints.newBuilder()
                .setCollectionName(collectionName)
                .setPoints(PointsSelector.newBuilder()
                        .setPoints(PointsIdsList.newBuilder()
                                .addIds(id(characterId))
                                .build())
                        .build())
                .build();

        try {
            qdrantClient.delete(deleteRequest);
        } catch (StatusRuntimeException e) {
            Thread.currentThread().interrupt();
            throw new TechnicalException("Failed to delete vector for character " + characterId);
        }
    }

    @Override
    public List<UUID> search(List<UUID> candidateIds, float[] queryVector, int topK) {

        var filter = Filter.newBuilder()
                .addAllShould(candidateIds.stream()
                        .map(UUID::toString)
                        .map(id -> matchKeyword("characterId", id))
                        .toList())
                .build();

        var searchRequest = SearchPoints.newBuilder()
                .setCollectionName(collectionName)
                .addAllVector(toFloatList(queryVector))
                .setFilter(filter)
                .setLimit(topK)
                .setWithPayload(WithPayloadSelector.newBuilder().setEnable(true).build())
                .build();

        try {
            var response = qdrantClient.search(searchRequest);

            return response.getResultList().stream()
                    .filter(r -> r.getPayloadMap().containsKey("characterId"))
                    .map(r -> UUID.fromString(r.getPayloadMap().get("characterId").getStringValue()))
                    .toList();
        } catch (StatusRuntimeException e) {
            Thread.currentThread().interrupt();
            throw new TechnicalException("Failed to search vectors for character candidates");
        }
    }
}
