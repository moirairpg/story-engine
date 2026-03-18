package me.moirai.storyengine.core.port.outbound.generation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record TextModerationResult(
        boolean isContentFlagged,
        Map<String, Double> moderationScores,
        List<String> flaggedTopics) {

    public TextModerationResult {
        moderationScores = Collections.unmodifiableMap(moderationScores);
        flaggedTopics = Collections.unmodifiableList(flaggedTopics);
    }
}
