package me.moirai.storyengine.core.port.outbound.generation;

import java.util.List;
import java.util.Map;

public class TextModerationResultFixture {

    public static TextModerationResult withFlags() {

        return new TextModerationResult(
                true,
                Map.of("violence", 0.6, "sexual", 1.1),
                List.of("violence", "sexual"));
    }

    public static TextModerationResult withoutFlags() {

        return new TextModerationResult(
                false,
                Map.of(),
                List.of());
    }
}
