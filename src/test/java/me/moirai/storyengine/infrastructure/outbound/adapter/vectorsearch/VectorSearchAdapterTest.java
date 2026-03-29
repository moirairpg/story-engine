package me.moirai.storyengine.infrastructure.outbound.adapter.vectorsearch;

import static io.qdrant.client.ValueFactory.value;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Points.DeletePoints;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.SearchPoints;
import io.qdrant.client.grpc.Points.SearchResponse;
import io.qdrant.client.grpc.Points.UpsertPoints;
import io.qdrant.client.grpc.PointsGrpc.PointsBlockingStub;

@ExtendWith(MockitoExtension.class)
class VectorSearchAdapterTest {

    @Mock(answer = Answers.RETURNS_MOCKS)
    private QdrantGrpcClient grpcClient;

    @Mock
    private PointsBlockingStub pointsStub;

    @InjectMocks
    private VectorSearchAdapter adapter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adapter, "qdrantClient", pointsStub);
        ReflectionTestUtils.setField(adapter, "collectionName", "lorebook");
    }

    @Test
    void shouldUpsertPointWhenCalledWithValidArgs() {

        // given
        var adventureId = UUID.randomUUID();
        var entryId = UUID.randomUUID();
        var vector = new float[] { 0.1f, 0.2f, 0.3f };

        // when
        adapter.upsert(adventureId, entryId, vector);

        // then
        verify(pointsStub).upsert(any(UpsertPoints.class));
    }

    @Test
    void shouldDeletePointWhenCalledWithEntryId() {

        // given
        var entryId = UUID.randomUUID();

        // when
        adapter.delete(entryId);

        // then
        verify(pointsStub).delete(any(DeletePoints.class));
    }

    @Test
    void shouldReturnEntryIdsWhenSearchReturnsResults() {

        // given
        var adventureId = UUID.randomUUID();
        var entryId = UUID.randomUUID();
        var vector = new float[] { 0.1f, 0.2f, 0.3f };

        var scoredPoint = ScoredPoint.newBuilder()
                .putPayload("entryId", value(entryId.toString()))
                .build();

        when(pointsStub.search(any(SearchPoints.class)))
                .thenReturn(SearchResponse.newBuilder().addResult(scoredPoint).build());

        // when
        var result = adapter.search(adventureId, vector, 5);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(entryId);
    }

    @Test
    void shouldReturnEmptyListWhenSearchReturnsNoResults() {

        // given
        var adventureId = UUID.randomUUID();
        var vector = new float[] { 0.1f, 0.2f };

        when(pointsStub.search(any(SearchPoints.class)))
                .thenReturn(SearchResponse.getDefaultInstance());

        // when
        var result = adapter.search(adventureId, vector, 5);

        // then
        assertThat(result).isEmpty();
    }
}
