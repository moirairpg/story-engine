package me.moirai.storyengine.core.port;

import me.moirai.storyengine.core.port.outbound.TextGenerationRequest;
import me.moirai.storyengine.core.port.outbound.TextGenerationResult;
import reactor.core.publisher.Mono;

public interface TextCompletionPort {

    Mono<TextGenerationResult> generateTextFrom(TextGenerationRequest request);
}
