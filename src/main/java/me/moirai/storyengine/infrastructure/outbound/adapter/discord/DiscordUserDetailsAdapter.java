package me.moirai.storyengine.infrastructure.outbound.adapter.discord;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import me.moirai.storyengine.common.exception.OpenAiApiException;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDataResponse;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDetailsPort;
import me.moirai.storyengine.infrastructure.outbound.adapter.generation.CompletionResponseError;
import tools.jackson.databind.json.JsonMapper;

@Component
public class DiscordUserDetailsAdapter implements DiscordUserDetailsPort {

    private static final Logger LOG = LoggerFactory.getLogger(DiscordUserDetailsAdapter.class);

    private static final String BEARER = "Bearer %s";
    private static final String AUTHENTICATION_ERROR = "Error authenticating user on Discord";
    private static final String UNKNOWN_ERROR = "Error on Discord API";
    private static final String BAD_REQUEST_ERROR = "Bad request calling Discord API";

    private static final Predicate<HttpStatusCode> BAD_REQUEST_PREDICATE = statusCode -> statusCode
            .isSameCodeAs(HttpStatusCode.valueOf(400));

    private static final Predicate<HttpStatusCode> UNAUTHORIZED_PREDICATE = statusCode -> statusCode
            .isSameCodeAs(HttpStatusCode.valueOf(401));

    private final String usersUri;
    private final JsonMapper jsonMapper;
    private final RestClient discordClient;

    public DiscordUserDetailsAdapter(
            @Value("${moirai.discord.api.users-uri}") String usersUri,
            RestClient discordClient,
            JsonMapper jsonMapper) {

        this.jsonMapper = jsonMapper;
        this.usersUri = usersUri;
        this.discordClient = discordClient;
    }

    @Override
    public Optional<DiscordUserDataResponse> getUserById(String userDiscordId, String token) {

        return Optional.ofNullable(discordClient.get()
                .uri(format(usersUri, userDiscordId))
                .headers(headers -> {
                    headers.add(AUTHORIZATION, format(BEARER, token));
                    headers.add(CONTENT_TYPE, APPLICATION_JSON_VALUE);
                })
                .retrieve()
                .onStatus(UNAUTHORIZED_PREDICATE, this::handleUnauthorized)
                .onStatus(BAD_REQUEST_PREDICATE, this::handleBadRequest)
                .onStatus(HttpStatusCode::isError, this::handleUnknownError)
                .body(DiscordUserDataResponse.class));
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
        return jsonMapper.readValue(response.getBody(), CompletionResponseError.class);
    }
}
