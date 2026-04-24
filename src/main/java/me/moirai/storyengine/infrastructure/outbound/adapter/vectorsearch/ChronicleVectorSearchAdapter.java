package me.moirai.storyengine.infrastructure.outbound.adapter.vectorsearch;

import static io.qdrant.client.ConditionFactory.matchKeyword;
import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.grpc.StatusRuntimeException;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Points.DeletePoints;
import io.qdrant.client.grpc.Common.Filter;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.PointsSelector;
import io.qdrant.client.grpc.Points.SearchPoints;
import io.qdrant.client.grpc.Points.UpsertPoints;
import io.qdrant.client.grpc.Points.WithPayloadSelector;
import io.qdrant.client.grpc.PointsGrpc;
import io.qdrant.client.grpc.PointsGrpc.PointsBlockingStub;
import me.moirai.storyengine.common.exception.TechnicalException;
import me.moirai.storyengine.core.port.outbound.vectorsearch.ChronicleVectorSearchPort;

@Component
public class ChronicleVectorSearchAdapter implements ChronicleVectorSearchPort {

    private final PointsBlockingStub qdrantClient;
    private final String collectionName;

    public ChronicleVectorSearchAdapter(
            QdrantGrpcClient qdrantClient,
            @Value("${moirai.rag.chronicle.collection-name}") String collectionName) {

        this.qdrantClient = PointsGrpc.newBlockingStub(qdrantClient.channel());
        this.collectionName = collectionName;
    }

    @Override
    public void upsert(UUID adventureId, UUID segmentId, float[] vector) {

        var payload = Map.of(
                "adventureId", value(adventureId.toString()),
                "segmentId", value(segmentId.toString()));

        var point = PointStruct.newBuilder()
                .setId(id(segmentId))
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
            throw new TechnicalException("Failed to upsert vector for segment " + segmentId);
        }
    }

    @Override
    public List<UUID> search(UUID adventureId, float[] queryVector, int topK) {

        var filter = Filter.newBuilder()
                .addMust(matchKeyword("adventureId", adventureId.toString()))
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
                    .filter(r -> r.getPayloadMap().containsKey("segmentId"))
                    .map(r -> UUID.fromString(r.getPayloadMap().get("segmentId").getStringValue()))
                    .toList();
        } catch (StatusRuntimeException e) {
            Thread.currentThread().interrupt();
            throw new TechnicalException("Failed to search vectors for adventure " + adventureId);
        }
    }

    @Override
    public void deleteAllByAdventureId(UUID adventureId) {

        var filter = Filter.newBuilder()
                .addMust(matchKeyword("adventureId", adventureId.toString()))
                .build();

        var deleteRequest = DeletePoints.newBuilder()
                .setCollectionName(collectionName)
                .setPoints(PointsSelector.newBuilder()
                        .setFilter(filter)
                        .build())
                .build();

        try {
            qdrantClient.delete(deleteRequest);
        } catch (StatusRuntimeException e) {
            Thread.currentThread().interrupt();
            throw new TechnicalException("Failed to delete vectors for adventure " + adventureId);
        }
    }

    private List<Float> toFloatList(float[] vector) {

        var list = new ArrayList<Float>(vector.length);
        for (var v : vector) {
            list.add(v);
        }

        return list;
    }
}
