package me.moirai.storyengine.core.port.outbound.generation;

public interface TextCompletionPort {

    TextGenerationResult generateTextFrom(TextGenerationRequest request);
}
