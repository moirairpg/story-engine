package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmbeddingData {

    @JsonProperty("embedding")
    private float[] embedding;

    public float[] getEmbedding() {
        return embedding;
    }
}
