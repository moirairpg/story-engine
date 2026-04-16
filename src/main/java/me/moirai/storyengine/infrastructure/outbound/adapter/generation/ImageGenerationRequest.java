package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ImageGenerationRequest(
        @JsonProperty("model") String model,
        @JsonProperty("prompt") String prompt,
        @JsonProperty("quality") String quality) {
}
