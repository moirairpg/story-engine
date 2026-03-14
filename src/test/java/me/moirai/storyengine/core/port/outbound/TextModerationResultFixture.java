package me.moirai.storyengine.core.port.outbound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextModerationResultFixture {

    public static TextModerationResult.Builder withFlags() {

        List<String> flaggedTopics = new ArrayList<>();
        flaggedTopics.add("violence");
        flaggedTopics.add("sexual");

        Map<String, Double> flaggedTopicScores = new HashMap<>();
        flaggedTopicScores.put("violence", 0.6);
        flaggedTopicScores.put("sexual", 1.1);

        return TextModerationResult.builder()
                .contentFlagged(true)
                .flaggedTopics(flaggedTopics)
                .moderationScores(flaggedTopicScores);
    }

    public static TextModerationResult.Builder withoutFlags() {

        List<String> flaggedTopics = new ArrayList<>();
        Map<String, Double> flaggedTopicScores = new HashMap<>();

        return TextModerationResult.builder()
                .contentFlagged(false)
                .flaggedTopics(flaggedTopics)
                .moderationScores(flaggedTopicScores);
    }
}
