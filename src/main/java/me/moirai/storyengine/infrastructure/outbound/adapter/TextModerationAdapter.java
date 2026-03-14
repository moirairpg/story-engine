package me.moirai.storyengine.infrastructure.outbound.adapter;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
import me.moirai.storyengine.core.application.model.result.TextModerationResult;
import me.moirai.storyengine.core.port.TextModerationPort;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.ModerationRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.response.CompletionResponseError;
import me.moirai.storyengine.infrastructure.outbound.adapter.response.ModerationResponse;
import reactor.core.publisher.Mono;

@Component
public class TextModerationAdapter implements TextModerationPort {

    private static final Logger LOG = LoggerFactory.getLogger(TextModerationAdapter.class);

    private static final String AUTHENTICATION_ERROR = "Error authenticating user on OpenAI";
    private static final String UNKNOWN_ERROR = "Error on OpenAI Moderation API";
    private static final String BAD_REQUEST_ERROR = "Bad request calling OpenAI Moderation API";

    private static final Predicate<HttpStatusCode> BAD_REQUEST = statusCode -> statusCode
            .isSameCodeAs(HttpStatusCode.valueOf(400));

    private static final Predicate<HttpStatusCode> UNAUTHORIZED = statusCode -> statusCode
            .isSameCodeAs(HttpStatusCode.valueOf(401));

    private final String moderationUrl;
    private final WebClient webClient;

    public TextModerationAdapter(@Value("${moirai.openai.api.base-url}") String baseUrl,
            @Value("${moirai.openai.api.moderation-uri}") String moderationUrl,
            @Value("${moirai.openai.api.token}") String token,
            WebClient.Builder webClientBuilder) {

        this.moderationUrl = moderationUrl;
        this.webClient = webClientBuilder.baseUrl(baseUrl)
                .defaultHeaders(headers -> {
                    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                    headers.add(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE);
                })
                .build();
    }

    @Override
    public Mono<TextModerationResult> moderate(String text) {

        ModerationRequest request = ModerationRequest.build(text);
        return webClient.post()
                .uri(moderationUrl)
                .bodyValue(request)
                .retrieve()
                .onStatus(UNAUTHORIZED, this::handleUnauthorized)
                .onStatus(BAD_REQUEST, this::handleBadRequest)
                .onStatus(HttpStatusCode::isError, this::handleUnknownError)
                .bodyToMono(ModerationResponse.class)
                .map(this::toResult);
    }

    private TextModerationResult toResult(ModerationResponse response) {

        return TextModerationResult.builder()
                .contentFlagged(response.getResults().get(0).getFlagged())
                .flaggedTopics(response.getResults()
                        .get(0)
                        .getCategories()
                        .entrySet()
                        .stream()
                        .filter(this::isTopicFlagged)
                        .map(Entry::getKey)
                        .toList())
                .moderationScores(response.getResults()
                        .get(0)
                        .getCategoryScores()
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> Double.valueOf(entry.getValue()))))
                .build();
    }

    private boolean isTopicFlagged(Entry<String, Boolean> entry) {
        return entry.getValue();
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
