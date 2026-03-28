package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestClient;

import me.moirai.storyengine.common.enums.AiRole;
import me.moirai.storyengine.core.port.outbound.generation.TextCompletionPort;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationRequest;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationResult;

@Component
public class TextCompletionAdapter implements TextCompletionPort {

    private final String token;
    private final String responsesUri;
    private final RestClient openAiClient;

    public TextCompletionAdapter(
            @Value("${moirai.openai.api.responses-uri}") String responsesUri,
            @Value("${moirai.openai.api.token}") String token,
            RestClient openAiClient) {

        this.token = token;
        this.responsesUri = responsesUri;
        this.openAiClient = openAiClient;
    }

    @Override
    public TextGenerationResult generateTextFrom(TextGenerationRequest request) {

        var response = openAiClient.post()
                .uri(responsesUri)
                .headers(headers -> {
                    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                    headers.add(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE);
                })
                .body(toRequest(request))
                .retrieve()
                .body(OpenAiResponsesApiResponse.class);

        return toResult(response);
    }

    private OpenAiResponsesApiRequest toRequest(TextGenerationRequest request) {

        var input = request.messages().stream()
                .map(m -> new OpenAiInputMessage(toApiRole(m.role()), m.content()))
                .toList();

        return OpenAiResponsesApiRequest.builder()
                .model(request.model())
                .instructions(request.instructions())
                .input(input)
                .temperature(request.temperature())
                .maxOutputTokens(request.maxTokens())
                .build();
    }

    private String toApiRole(AiRole role) {
        return switch (role) {
            case SYSTEM -> "developer";
            case USER -> "user";
            case ASSISTANT -> "assistant";
        };
    }

    private TextGenerationResult toResult(OpenAiResponsesApiResponse response) {

        var outputText = response.getOutput().get(0).getContent().get(0).getText();

        return TextGenerationResult.builder()
                .completionTokens(response.getUsage().getOutputTokens())
                .promptTokens(response.getUsage().getInputTokens())
                .totalTokens(response.getUsage().getTotalTokens())
                .outputText(outputText)
                .build();
    }
}
