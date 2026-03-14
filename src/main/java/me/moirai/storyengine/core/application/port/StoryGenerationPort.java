package me.moirai.storyengine.core.application.port;

import me.moirai.storyengine.core.port.outbound.StoryGenerationRequest;
import reactor.core.publisher.Mono;

public interface StoryGenerationPort {

    Mono<Void> continueStory(StoryGenerationRequest useCase);
}
