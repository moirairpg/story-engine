package me.moirai.storyengine.infrastructure.outbound.adapter;

import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import me.moirai.storyengine.common.exception.OpenAiApiException;
import me.moirai.storyengine.core.application.model.request.TextGenerationRequest;
import me.moirai.storyengine.core.application.model.result.TextGenerationResult;
import me.moirai.storyengine.core.port.TextCompletionPort;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.ChatMessage;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.CompletionRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.response.CompletionResponse;
import me.moirai.storyengine.infrastructure.outbound.adapter.response.CompletionResponseError;
import reactor.core.publisher.Mono;

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

    private final String completionsUri;
    private final WebClient webClient;

    public TextCompletionAdapter(@Value("${moirai.openai.api.base-url}") String baseUrl,
            @Value("${moirai.openai.api.completions-uri}") String completionsUri,
            @Value("${moirai.openai.api.token}") String token,
            WebClient.Builder webClientBuilder) {

        this.completionsUri = completionsUri;
        this.webClient = webClientBuilder.baseUrl(baseUrl)
                .defaultHeaders(headers -> {
                    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                    headers.add(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE);
                })
                .build();
    }

    @Override
    public Mono<TextGenerationResult> generateTextFrom(TextGenerationRequest request) {

        return webClient.post()
                .uri(completionsUri)
                .bodyValue(toRequest(request))
                .retrieve()
                .onStatus(UNAUTHORIZED, this::handleUnauthorized)
                .onStatus(BAD_REQUEST, this::handleBadRequest)
                .onStatus(HttpStatusCode::isError, this::handleUnknownError)
                .bodyToMono(CompletionResponse.class)
                .map(this::toResult);
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
                        .map(message -> ChatMessage.build(message.getRole().name().toLowerCase(), message.getContent()))
                        .toList())
                .build();
    }

    private TextGenerationResult toResult(CompletionResponse response) {

        return TextGenerationResult.builder()
                .completionTokens(response.getUsage().getCompletionTokens())
                .promptTokens(response.getUsage().getPromptTokens())
                .totalTokens(response.getUsage().getTotalTokens())
                .outputText(response.getChoices().get(0).getMessage().getContent())
                .build();
    }

    private Mono<? extends Throwable> handleUnauthorized(ClientResponse clientResponse) {

        return Mono.error(new OpenAiApiException(HttpStatus.UNAUTHORIZED, AUTHENTICATION_ERROR));
    }

    private Mono<? extends Throwable> handleBadRequest(ClientResponse clientResponse) {

        return clientResponse.bodyToMono(CompletionResponseError.class)
                .map(resp -> {
                    LOG.error(BAD_REQUEST_ERROR + " -> {}", resp);
                    return new OpenAiApiException(HttpStatus.BAD_REQUEST, resp.getType(), resp.getMessage(),
                            String.format(BAD_REQUEST_ERROR, resp.getType(), resp.getMessage()));
                });
    }

    private Mono<? extends Throwable> handleUnknownError(ClientResponse clientResponse) {

        return clientResponse.bodyToMono(CompletionResponseError.class)
                .map(resp -> {
                    LOG.error(UNKNOWN_ERROR + " -> {}", resp);
                    return new OpenAiApiException(HttpStatus.INTERNAL_SERVER_ERROR, resp.getType(), resp.getMessage(),
                            String.format(UNKNOWN_ERROR, resp.getType(), resp.getMessage()));
                });
    }
}
