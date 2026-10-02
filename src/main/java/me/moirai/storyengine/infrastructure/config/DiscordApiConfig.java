package me.moirai.storyengine.infrastructure.config;

import static java.nio.charset.StandardCharsets.UTF_8;

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

import me.moirai.storyengine.common.exception.AuthenticationFailedException;
import me.moirai.storyengine.common.exception.RestException;
import me.moirai.storyengine.infrastructure.outbound.adapter.discord.DiscordAuthenticationAdapter;

@Configuration
public class DiscordApiConfig {

    private static final Logger LOG = LoggerFactory.getLogger(DiscordAuthenticationAdapter.class);

    private static final String AUTHENTICATION_ERROR = "Error authenticating user on Discord";
    private static final String UNKNOWN_ERROR = "Something went wrong. Contact support.";
    private static final String BAD_REQUEST_ERROR = "Bad request calling Discord API";

    private static final Predicate<HttpStatusCode> BAD_REQUEST_PREDICATE = statusCode -> statusCode
            .isSameCodeAs(HttpStatusCode.valueOf(400));

    private static final Predicate<HttpStatusCode> UNAUTHORIZED_PREDICATE = statusCode -> statusCode
            .isSameCodeAs(HttpStatusCode.valueOf(401));

    private final String baseUrl;

    public DiscordApiConfig(@Value("${moirai.discord.api.base-url}") String baseUrl) {

        this.baseUrl = baseUrl;
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

        LOG.error(AUTHENTICATION_ERROR + " -> {}", readBody(response));
        throw new AuthenticationFailedException(AUTHENTICATION_ERROR);
    }

    private void handleBadRequest(HttpRequest request, ClientHttpResponse response) throws IOException {

        LOG.error(BAD_REQUEST_ERROR + " -> {}", readBody(response));
        throw new RestException(HttpStatus.BAD_REQUEST, BAD_REQUEST_ERROR);
    }

    private void handleUnknownError(HttpRequest request, ClientHttpResponse response) throws IOException {

        LOG.error(UNKNOWN_ERROR + " -> {}", readBody(response));
        throw new RestException(HttpStatus.INTERNAL_SERVER_ERROR, UNKNOWN_ERROR);
    }

    private String readBody(ClientHttpResponse response) throws IOException {
        return new String(response.getBody().readAllBytes(), UTF_8);
    }
}
