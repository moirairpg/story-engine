package me.moirai.storyengine.core.port.outbound.generation;

public interface StoryGenerationPort {

    void continueStory(StoryGenerationRequest useCase);
}
