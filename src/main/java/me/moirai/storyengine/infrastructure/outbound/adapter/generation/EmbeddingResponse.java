package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmbeddingResponse {

    @JsonProperty("data")
    private List<EmbeddingData> data;

    public List<EmbeddingData> getData() {
        return data;
    }
}
