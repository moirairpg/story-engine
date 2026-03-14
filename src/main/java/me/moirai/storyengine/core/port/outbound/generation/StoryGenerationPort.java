package me.moirai.storyengine.core.port.outbound.generation;

import reactor.core.publisher.Mono;

public interface StoryGenerationPort {

    Mono<Void> continueStory(StoryGenerationRequest useCase);
}
