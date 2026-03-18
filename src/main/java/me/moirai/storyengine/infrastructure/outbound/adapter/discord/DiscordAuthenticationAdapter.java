package me.moirai.storyengine.infrastructure.outbound.adapter.discord;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.util.Map;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.ResponseSpec;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import me.moirai.storyengine.common.exception.OpenAiApiException;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.AuthenticateUserResult;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthRequest;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;
import me.moirai.storyengine.core.port.outbound.discord.DiscordTokenRevocationRequest;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDataResponse;
import me.moirai.storyengine.core.port.outbound.discord.RefreshSessionTokenRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.generation.CompletionResponseError;

@Component
public class DiscordAuthenticationAdapter implements DiscordAuthenticationPort {

    private static final Logger LOG = LoggerFactory.getLogger(DiscordAuthenticationAdapter.class);

    private static final String BEARER = "Bearer %s";
    private static final String CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded";
    private static final String AUTHENTICATION_ERROR = "Error authenticating user on Discord";
    private static final String UNKNOWN_ERROR = "Error on Discord API";
    private static final String BAD_REQUEST_ERROR = "Bad request calling Discord API";

    private static final Predicate<HttpStatusCode> BAD_REQUEST_PREDICATE = statusCode -> statusCode
            .isSameCodeAs(HttpStatusCode.valueOf(400));

    private static final Predicate<HttpStatusCode> UNAUTHORIZED_PREDICATE = statusCode -> statusCode
            .isSameCodeAs(HttpStatusCode.valueOf(401));

    private final String usersUri;
    private final String tokenUri;
    private final String tokenRevokeUri;
    private final ObjectMapper objectMapper;
    private final RestClient discordClient;

    public DiscordAuthenticationAdapter(
            @Value("${moirai.discord.api.base-url}") String discordBaseUrl,
            @Value("${moirai.discord.api.users-uri}") String usersUri,
            @Value("${moirai.discord.api.token-uri}") String tokenUri,
            @Value("${moirai.discord.api.token-revoke-uri}") String tokenRevokeUri,
            RestClient discordClient,
            ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
        this.usersUri = usersUri;
        this.tokenUri = tokenUri;
        this.tokenRevokeUri = tokenRevokeUri;
        this.discordClient = discordClient;
    }

    @Override
    public AuthenticateUserResult authenticate(DiscordAuthRequest request) {

        var response = postForAuthentication(tokenUri, request)
                .body(DiscordAuthResponse.class);

        return toResult(response);
    }

    @Override
    public AuthenticateUserResult refreshSessionToken(RefreshSessionTokenRequest request) {

        var response = postForAuthentication(tokenUri, request)
                .body(DiscordAuthResponse.class);

        return toResult(response);
    }

    @Override
    public DiscordUserDataResponse retrieveLoggedUser(String token) {

        return discordClient.get()
                .uri(format(usersUri, "@me"))
                .headers(headers -> {
                    headers.add(AUTHORIZATION, format(BEARER, token));
                    headers.add(CONTENT_TYPE, APPLICATION_JSON_VALUE);
                })
                .retrieve()
                .onStatus(UNAUTHORIZED_PREDICATE, this::handleUnauthorized)
                .onStatus(BAD_REQUEST_PREDICATE, this::handleBadRequest)
                .onStatus(HttpStatusCode::isError, this::handleUnknownError)
                .body(DiscordUserDataResponse.class);
    }

    @Override
    public void logout(String clientId, String clientSecret, String token, String tokenTypeHint) {

        var request = new DiscordTokenRevocationRequest(
                clientId,
                clientSecret,
                token,
                tokenTypeHint);

        postForAuthentication(tokenRevokeUri, request)
                .body(Void.class);
    }

    private AuthenticateUserResult toResult(DiscordAuthResponse response) {

        return new AuthenticateUserResult(
                response.accessToken(),
                response.expiresIn(),
                response.refreshToken(),
                response.scope(),
                response.tokenType());
    }

    private ResponseSpec postForAuthentication(String url, Object request) {

        var valueMap = new LinkedMultiValueMap<String, String>();
        var fieldMap = objectMapper.convertValue(request, new TypeReference<Map<String, String>>() {
        });

        valueMap.setAll(fieldMap);

        return discordClient.post()
                .uri(url)
                .headers(headers -> {
                    headers.add(CONTENT_TYPE, CONTENT_TYPE_VALUE);
                    headers.add(ACCEPT, CONTENT_TYPE_VALUE);
                })
                .body(valueMap)
                .retrieve()
                .onStatus(UNAUTHORIZED_PREDICATE, this::handleUnauthorized)
                .onStatus(BAD_REQUEST_PREDICATE, this::handleBadRequest)
                .onStatus(HttpStatusCode::isError, this::handleUnknownError);
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
