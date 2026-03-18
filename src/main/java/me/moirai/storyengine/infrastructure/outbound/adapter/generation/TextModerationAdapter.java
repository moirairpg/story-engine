package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.moirai.storyengine.common.exception.OpenAiApiException;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationResult;

@Component
public class TextModerationAdapter implements TextModerationPort {

    private static final Logger LOG = LoggerFactory.getLogger(ReactiveTextModerationAdapter.class);

    private static final String AUTHENTICATION_ERROR = "Error authenticating user on OpenAI";
    private static final String UNKNOWN_ERROR = "Error on OpenAI Moderation API";
    private static final String BAD_REQUEST_ERROR = "Bad request calling OpenAI Moderation API";

    private static final Predicate<HttpStatusCode> BAD_REQUEST = statusCode -> statusCode
            .isSameCodeAs(HttpStatusCode.valueOf(400));

    private static final Predicate<HttpStatusCode> UNAUTHORIZED = statusCode -> statusCode
            .isSameCodeAs(HttpStatusCode.valueOf(401));

    private final String moderationUrl;
    private final RestClient openAiClient;
    private final ObjectMapper objectMapper;

    public TextModerationAdapter(
            @Value("${moirai.openai.api.moderation-uri}") String moderationUrl,
            RestClient openAiClient,
            ObjectMapper objectMapper) {

        this.moderationUrl = moderationUrl;
        this.openAiClient = openAiClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public TextModerationResult moderate(String text) {

        var request = new ModerationRequest(text);
        var response = openAiClient.post()
                .uri(moderationUrl)
                .body(request)
                .retrieve()
                .onStatus(UNAUTHORIZED, this::handleUnauthorized)
                .onStatus(BAD_REQUEST, this::handleBadRequest)
                .onStatus(HttpStatusCode::isError, this::handleUnknownError)
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

    private void handleUnauthorized(HttpRequest request, ClientHttpResponse response) throws IOException {
        throw new OpenAiApiException(HttpStatus.UNAUTHORIZED, AUTHENTICATION_ERROR);
    }

    private void handleBadRequest(HttpRequest request, ClientHttpResponse response) throws IOException {

        var error = mapErrorResponse(response);
        LOG.error(BAD_REQUEST_ERROR + " -> {}", error);
        throw new OpenAiApiException(HttpStatus.BAD_REQUEST, error.getType(), error.getMessage(),
                String.format(BAD_REQUEST_ERROR, error.getType(), error.getMessage()));
    }

    private void handleUnknownError(HttpRequest request, ClientHttpResponse response) throws IOException {

        var error = mapErrorResponse(response);
        LOG.error(UNKNOWN_ERROR + " -> {}", error);
        throw new OpenAiApiException(HttpStatus.INTERNAL_SERVER_ERROR, error.getType(), error.getMessage(),
                String.format(UNKNOWN_ERROR, error.getType(), error.getMessage()));
    }

    private CompletionResponseError mapErrorResponse(ClientHttpResponse response) throws IOException {
        return objectMapper.readValue(response.getBody(), CompletionResponseError.class);
    }
}
