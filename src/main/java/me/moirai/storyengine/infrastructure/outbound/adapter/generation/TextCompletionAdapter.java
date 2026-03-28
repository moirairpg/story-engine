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

import tools.jackson.databind.json.JsonMapper;
import me.moirai.storyengine.common.enums.AiRole;
import me.moirai.storyengine.common.exception.RestException;
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
    private final String responsesUri;
    private final RestClient openAiClient;
    private final JsonMapper jsonMapper;

    public TextCompletionAdapter(
            @Value("${moirai.openai.api.responses-uri}") String responsesUri,
            @Value("${moirai.openai.api.token}") String token,
            RestClient openAiClient,
            JsonMapper jsonMapper) {

        this.token = token;
        this.responsesUri = responsesUri;
        this.openAiClient = openAiClient;
        this.jsonMapper = jsonMapper;
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
                .onStatus(UNAUTHORIZED, this::handleUnauthorized)
                .onStatus(BAD_REQUEST, this::handleBadRequest)
                .onStatus(HttpStatusCode::isError, this::handleUnknownError)
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

    private void handleUnauthorized(HttpRequest request, ClientHttpResponse response) throws IOException {
        throw new RestException(HttpStatus.UNAUTHORIZED, AUTHENTICATION_ERROR);
    }

    private void handleBadRequest(HttpRequest request, ClientHttpResponse response) throws IOException {

        var error = mapErrorResponse(response);
        LOG.error(BAD_REQUEST_ERROR + " -> {}", error);
        throw new RestException(HttpStatus.BAD_REQUEST, error.getType(), error.getMessage(),
                String.format(BAD_REQUEST_ERROR, error.getType(), error.getMessage()));
    }

    private void handleUnknownError(HttpRequest request, ClientHttpResponse response) throws IOException {

        var error = mapErrorResponse(response);
        LOG.error(UNKNOWN_ERROR + " -> {}", error);
        throw new RestException(HttpStatus.INTERNAL_SERVER_ERROR, error.getType(), error.getMessage(),
                String.format(UNKNOWN_ERROR, error.getType(), error.getMessage()));
    }

    private CompletionResponseError mapErrorResponse(ClientHttpResponse response) throws IOException {
        return jsonMapper.readValue(response.getBody(), CompletionResponseError.class);
    }
}
