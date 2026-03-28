package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationResult;

@Component
public class TextModerationAdapter implements TextModerationPort {

    private final String moderationUrl;
    private final RestClient openAiClient;

    public TextModerationAdapter(
            @Value("${moirai.openai.api.moderation-uri}") String moderationUrl,
            RestClient openAiClient) {

        this.moderationUrl = moderationUrl;
        this.openAiClient = openAiClient;
    }

    @Override
    public TextModerationResult moderate(String text) {

        var request = new ModerationRequest(text);
        var response = openAiClient.post()
                .uri(moderationUrl)
                .body(request)
                .retrieve()
                .body(ModerationResponse.class);

        return toResult(response);
    }

    private TextModerationResult toResult(ModerationResponse response) {

        var result = response.getResults().get(0);

        var flaggedTopics = result.getCategories()
                .entrySet()
                .stream()
                .filter(this::isTopicFlagged)
                .map(Entry::getKey)
                .toList();

        var moderationScores = result.getCategoryScores()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> Double.valueOf(entry.getValue())));

        return new TextModerationResult(result.getFlagged(), moderationScores, flaggedTopics);
    }

    private boolean isTopicFlagged(Entry<String, Boolean> entry) {
        return entry.getValue();
    }
}
