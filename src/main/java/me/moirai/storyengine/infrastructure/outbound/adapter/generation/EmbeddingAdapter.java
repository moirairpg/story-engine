package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import me.moirai.storyengine.core.port.outbound.generation.EmbeddingPort;

@Component
public class EmbeddingAdapter implements EmbeddingPort {

    private final String embeddingsUri;
    private final String embeddingModel;
    private final RestClient openAiClient;

    public EmbeddingAdapter(
            @Value("${moirai.openai.api.embeddings-uri}") String embeddingsUri,
            @Value("${moirai.openai.models.embedding}") String embeddingModel,
            RestClient openAiClient) {

        this.embeddingsUri = embeddingsUri;
        this.embeddingModel = embeddingModel;
        this.openAiClient = openAiClient;
    }

    @Override
    public float[] embed(String text) {

        var request = new EmbeddingRequest(List.of(text), embeddingModel);
        var response = openAiClient.post()
                .uri(embeddingsUri)
                .body(request)
                .retrieve()
                .body(EmbeddingResponse.class);

        return response.getData().get(0).getEmbedding();
    }

    @Override
    public List<float[]> embedAll(List<String> texts) {

        var request = new EmbeddingRequest(texts, embeddingModel);
        var response = openAiClient.post()
                .uri(embeddingsUri)
                .body(request)
                .retrieve()
                .body(EmbeddingResponse.class);

        return response.getData()
                .stream()
                .map(EmbeddingData::getEmbedding)
                .toList();
    }
}
