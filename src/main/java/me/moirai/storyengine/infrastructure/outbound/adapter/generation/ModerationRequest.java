package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import java.util.Arrays;
import java.util.List;

public record ModerationRequest(List<ModerationInput> input) {

    public record ModerationInput(String type, String text) {}

    public static ModerationRequest of(String... inputs) {
        return new ModerationRequest(Arrays.stream(inputs)
                .map(text -> new ModerationInput("text", text))
                .toList());
    }
}
