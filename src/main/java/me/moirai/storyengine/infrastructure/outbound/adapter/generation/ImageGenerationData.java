package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ImageGenerationData(
        @JsonProperty("b64_json") String b64Json) {
}
