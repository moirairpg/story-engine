package me.moirai.storyengine.infrastructure.outbound.adapter.character;

import static io.qdrant.client.ValueFactory.value;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Common.Condition;
import io.qdrant.client.grpc.Points.DeletePoints;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.SearchPoints;
import io.qdrant.client.grpc.Points.SearchResponse;
import io.qdrant.client.grpc.Points.UpsertPoints;
import io.qdrant.client.grpc.PointsGrpc.PointsBlockingStub;

@ExtendWith(MockitoExtension.class)
public class PlayerCharacterVectorSearchAdapterTest {

    @Mock(answer = Answers.RETURNS_MOCKS)
    private QdrantGrpcClient grpcClient;

    @Mock
    private PointsBlockingStub pointsStub;

    @InjectMocks
    private PlayerCharacterVectorSearchAdapter adapter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adapter, "qdrantClient", pointsStub);
        ReflectionTestUtils.setField(adapter, "collectionName", "player-character");
    }

    @Test
    void shouldUpsertPointWhenCalledWithValidArgs() {

        // given
        var playerId = UUID.randomUUID();
        var vector = new float[] { 0.1f, 0.2f, 0.3f };

        // when
        adapter.upsert(playerId, vector);

        // then
        verify(pointsStub).upsert(any(UpsertPoints.class));
    }

    @Test
    void shouldDeletePointWhenCalledWithEntryId() {

        // given
        var playerId = UUID.randomUUID();

        // when
        adapter.delete(playerId);

        // then
        verify(pointsStub).delete(any(DeletePoints.class));
    }

    @Test
    void shouldReturnEntryIdsWhenSearchReturnsResults() {

        // given
        var candidateIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        var vector = new float[] { 0.1f, 0.2f, 0.3f };

        var scoredPoint = ScoredPoint.newBuilder()
                .putPayload("characterId", value(candidateIds.get(0).toString()))
                .build();

        when(pointsStub.search(any(SearchPoints.class)))
                .thenReturn(SearchResponse.newBuilder().addResult(scoredPoint).build());

        // when
        var result = adapter.search(candidateIds, vector, 5);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(candidateIds.get(0));

        var searchCaptor = ArgumentCaptor.forClass(SearchPoints.class);
        verify(pointsStub).search(searchCaptor.capture());
        var filter = searchCaptor.getValue().getFilter();

        assertThat(filter.getMustCount()).isZero();
        assertThat(filter.getShouldCount()).isEqualTo(candidateIds.size());
        assertThat(filter.getShouldList())
                .extracting(Condition::getField)
                .allSatisfy(field -> assertThat(field.getKey()).isEqualTo("characterId"))
                .extracting(field -> field.getMatch().getKeyword())
                .containsExactlyInAnyOrderElementsOf(candidateIds.stream().map(UUID::toString).toList());
    }

    @Test
    void shouldReturnEmptyListWhenSearchReturnsNoResults() {

        // given
        var characterId = List.of(UUID.randomUUID(), UUID.randomUUID());
        var vector = new float[] { 0.1f, 0.2f };

        when(pointsStub.search(any(SearchPoints.class)))
                .thenReturn(SearchResponse.getDefaultInstance());

        // when
        var result = adapter.search(characterId, vector, 5);

        // then
        assertThat(result).isEmpty();
    }
}
