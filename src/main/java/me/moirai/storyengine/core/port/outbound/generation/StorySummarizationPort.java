package me.moirai.storyengine.core.port.outbound.generation;

import java.util.Map;

public interface StorySummarizationPort {

    Map<String, Object> summarizeContextWith(
            Map<String, Object> context,
            StoryGenerationRequest storyGenerationRequest);
}
