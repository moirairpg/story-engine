package me.moirai.storyengine.core.port;

import java.util.Map;

import me.moirai.storyengine.core.port.outbound.StoryGenerationRequest;
import reactor.core.publisher.Mono;

public interface StorySummarizationPort {

    Mono<Map<String, Object>> summarizeContextWith(Map<String, Object> context,
            StoryGenerationRequest storyGenerationRequest);
}
