package me.moirai.storyengine.core.port.outbound.generation;

public interface TextModerationPort {

    TextModerationResult moderate(String text);
}
