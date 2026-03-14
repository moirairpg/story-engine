package me.moirai.storyengine.core.port.outbound.generation;

import java.util.Map;

import reactor.core.publisher.Mono;

public interface StorySummarizationPort {

    Mono<Map<String, Object>> summarizeContextWith(Map<String, Object> context,
            StoryGenerationRequest storyGenerationRequest);
}
