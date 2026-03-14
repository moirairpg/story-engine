package me.moirai.storyengine.core.port;

import me.moirai.storyengine.core.application.model.request.TextGenerationRequest;
import me.moirai.storyengine.core.application.model.result.TextGenerationResult;
import reactor.core.publisher.Mono;

public interface TextCompletionPort {

    Mono<TextGenerationResult> generateTextFrom(TextGenerationRequest request);
}
