package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EmbeddingRequest(
        @JsonProperty("input") List<String> input,
        @JsonProperty("model") String model) {
}
