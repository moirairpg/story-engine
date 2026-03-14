package me.moirai.storyengine.core.application.port;

import java.util.Map;

import me.moirai.storyengine.infrastructure.outbound.adapter.request.StoryGenerationRequest;
import reactor.core.publisher.Mono;

public interface StorySummarizationPort {

    Mono<Map<String, Object>> summarizeContextWith(Map<String, Object> context,
            StoryGenerationRequest storyGenerationRequest);
}
