package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EmbeddingRequest(
        @JsonProperty("input") String input,
        @JsonProperty("model") String model) {
}
