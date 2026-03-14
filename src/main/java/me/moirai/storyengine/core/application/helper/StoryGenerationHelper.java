package me.moirai.storyengine.core.application.helper;

import me.moirai.storyengine.infrastructure.outbound.adapter.request.StoryGenerationRequest;
import reactor.core.publisher.Mono;

public interface StoryGenerationHelper {

    Mono<Void> continueStory(StoryGenerationRequest useCase);
}
