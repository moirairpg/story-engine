package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;

import me.moirai.storyengine.AbstractWebMockTest;

class EmbeddingAdapterTest extends AbstractWebMockTest {

    private EmbeddingAdapter adapter;

    @BeforeEach
    void before() {

        var restClient = RestClient.builder()
                .baseUrl("http://localhost:" + PORT)
                .build();

        adapter = new EmbeddingAdapter("/v1/embeddings", "text-embedding-3-small", restClient);
    }

    @Test
    void shouldReturnEmbeddingVectorWhenApiCallSucceeds() throws JsonProcessingException {

        // given
        var embeddingData = new EmbeddingData();
        var expectedEmbedding = new float[] { 0.1f, 0.2f, 0.3f };
        setEmbedding(embeddingData, expectedEmbedding);

        var embeddingResponse = new EmbeddingResponse();
        setData(embeddingResponse, List.of(embeddingData));

        prepareWebserverFor(embeddingResponse, 200);

        // when
        var result = adapter.embed("some text");

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result[0]).isEqualTo(0.1f);
        assertThat(result[1]).isEqualTo(0.2f);
        assertThat(result[2]).isEqualTo(0.3f);
    }

    @Test
    void shouldPropagateExceptionWhenApiCallFails() {

        // given
        prepareWebserverFor(500);

        // when / then
        assertThatThrownBy(() -> adapter.embed("some text"))
                .isInstanceOf(Exception.class);
    }

    private void setEmbedding(EmbeddingData data, float[] embedding) {
        try {
            var field = EmbeddingData.class.getDeclaredField("embedding");
            field.setAccessible(true);
            field.set(data, embedding);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setData(EmbeddingResponse response, java.util.List<EmbeddingData> dataList) {
        try {
            var field = EmbeddingResponse.class.getDeclaredField("data");
            field.setAccessible(true);
            field.set(response, dataList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
