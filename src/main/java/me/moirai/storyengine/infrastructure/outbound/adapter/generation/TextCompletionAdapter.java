package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import java.io.IOException;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.moirai.storyengine.common.dto.ChatMessage;
import me.moirai.storyengine.common.exception.OpenAiApiException;
import me.moirai.storyengine.core.port.outbound.generation.TextCompletionPort;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationRequest;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationResult;

@Component
public class TextCompletionAdapter implements TextCompletionPort {

    private static final Logger LOG = LoggerFactory.getLogger(TextCompletionAdapter.class);

    private static final String AUTHENTICATION_ERROR = "Error authenticating user on OpenAI";
    private static final String UNKNOWN_ERROR = "Error on OpenAI API";
    private static final String BAD_REQUEST_ERROR = "Bad request calling OpenAI API";

    private static final Predicate<HttpStatusCode> BAD_REQUEST = statusCode -> statusCode
            .isSameCodeAs(HttpStatusCode.valueOf(400));

    private static final Predicate<HttpStatusCode> UNAUTHORIZED = statusCode -> statusCode
            .isSameCodeAs(HttpStatusCode.valueOf(401));

    private final String token;
    private final String completionsUri;
    private final RestClient discordClient;
    private final ObjectMapper objectMapper;

    public TextCompletionAdapter(
            @Value("${moirai.openai.api.completions-uri}") String completionsUri,
            @Value("${moirai.openai.api.token}") String token,
            RestClient discordClient,
            ObjectMapper objectMapper) {

        this.token = token;
        this.completionsUri = completionsUri;
        this.discordClient = discordClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public TextGenerationResult generateTextFrom(TextGenerationRequest request) {

        var response = discordClient.post()
                .uri(completionsUri)
                .headers(headers -> {
                    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                    headers.add(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE);
                })
                .body(toRequest(request))
                .retrieve()
                .onStatus(UNAUTHORIZED, this::handleUnauthorized)
                .onStatus(BAD_REQUEST, this::handleBadRequest)
                .onStatus(HttpStatusCode::isError, this::handleUnknownError)
                .body(CompletionResponse.class);

        return toResult(response);
    }

    private CompletionRequest toRequest(TextGenerationRequest request) {

        return CompletionRequest.builder()
                .frequencyPenalty(request.getFrequencyPenalty())
                .presencePenalty(request.getPresencePenalty())
                .temperature(request.getTemperature())
                .logitBias(request.getLogitBias())
                .stop(request.getStopSequences())
                .maxTokens(request.getMaxTokens())
                .model(request.getModel())
                .messages(request.getMessages()
                        .stream()
                        .map(message -> new ChatMessage(message.role(), message.content()))
                        .toList())
                .build();
    }

    private TextGenerationResult toResult(CompletionResponse response) {

        return TextGenerationResult.builder()
                .completionTokens(response.getUsage().getCompletionTokens())
                .promptTokens(response.getUsage().getPromptTokens())
                .totalTokens(response.getUsage().getTotalTokens())
                .outputText(response.getChoices().get(0).getMessage().content())
                .build();
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