package me.moirai.storyengine.infrastructure.config;

import java.io.IOException;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestClient;

import me.moirai.storyengine.common.exception.RestException;
import me.moirai.storyengine.infrastructure.outbound.adapter.discord.DiscordAuthenticationAdapter;
import me.moirai.storyengine.infrastructure.outbound.adapter.generation.CompletionResponseError;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class DiscordApiConfig {

    private static final Logger LOG = LoggerFactory.getLogger(DiscordAuthenticationAdapter.class);

    private static final String AUTHENTICATION_ERROR = "Error authenticating user on Discord";
    private static final String UNKNOWN_ERROR = "Error on Discord API";
    private static final String BAD_REQUEST_ERROR = "Bad request calling Discord API";

    private static final Predicate<HttpStatusCode> BAD_REQUEST_PREDICATE = statusCode -> statusCode
            .isSameCodeAs(HttpStatusCode.valueOf(400));

    private static final Predicate<HttpStatusCode> UNAUTHORIZED_PREDICATE = statusCode -> statusCode
            .isSameCodeAs(HttpStatusCode.valueOf(401));

    private final String baseUrl;
    private final JsonMapper jsonMapper;

    public DiscordApiConfig(
            @Value("${moirai.discord.api.base-url}") String baseUrl,
            JsonMapper jsonMapper) {

        this.baseUrl = baseUrl;
        this.jsonMapper = jsonMapper;
    }

    @Bean
    RestClient discordClient() {

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
                .defaultStatusHandler(UNAUTHORIZED_PREDICATE, this::handleUnauthorized)
                .defaultStatusHandler(BAD_REQUEST_PREDICATE, this::handleBadRequest)
                .defaultStatusHandler(HttpStatusCode::isError, this::handleUnknownError)
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
