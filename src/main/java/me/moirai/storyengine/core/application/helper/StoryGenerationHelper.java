package me.moirai.storyengine.core.application.helper;

import me.moirai.storyengine.core.port.outbound.StoryGenerationRequest;
import reactor.core.publisher.Mono;

public interface StoryGenerationHelper {

    Mono<Void> continueStory(StoryGenerationRequest useCase);
}
