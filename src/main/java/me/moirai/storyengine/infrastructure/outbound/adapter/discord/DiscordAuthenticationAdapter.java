package me.moirai.storyengine.infrastructure.outbound.adapter.discord;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import java.util.Map;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import me.moirai.storyengine.common.exception.AuthenticationFailedException;
import me.moirai.storyengine.common.exception.DiscordApiException;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.AuthenticateUserResult;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthRequest;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;
import me.moirai.storyengine.core.port.outbound.discord.DiscordTokenRevocationRequest;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDataResponse;
import me.moirai.storyengine.core.port.outbound.discord.RefreshSessionTokenRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.response.DiscordAuthResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.DiscordErrorResponse;
import reactor.core.publisher.Mono;

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
    private final WebClient webClient;

    public DiscordAuthenticationAdapter(@Value("${moirai.discord.api.base-url}") String discordBaseUrl,
            @Value("${moirai.discord.api.users-uri}") String usersUri,
            @Value("${moirai.discord.api.token-uri}") String tokenUri,
            @Value("${moirai.discord.api.token-revoke-uri}") String tokenRevokeUri,
            WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
        this.usersUri = usersUri;
        this.tokenUri = tokenUri;
        this.tokenRevokeUri = tokenRevokeUri;
        this.webClient = webClientBuilder.baseUrl(discordBaseUrl).build();
    }

    @Override
    public Mono<AuthenticateUserResult> authenticate(DiscordAuthRequest request) {

        return postForAuthentication(tokenUri, request)
                .bodyToMono(DiscordAuthResponse.class)
                .map(this::toResult);
    }

    @Override
    public Mono<AuthenticateUserResult> refreshSessionToken(RefreshSessionTokenRequest request) {

        return postForAuthentication(tokenUri, request)
                .bodyToMono(DiscordAuthResponse.class)
                .map(this::toResult);
    }

    private AuthenticateUserResult toResult(DiscordAuthResponse response) {

        return AuthenticateUserResult.builder()
                .accessToken(response.getAccessToken())
                .refreshToken(response.getRefreshToken())
                .expiresIn(response.getExpiresIn())
                .tokenType(response.getTokenType())
                .scope(response.getScope())
                .build();
    }

    @Override
    public Mono<DiscordUserDataResponse> retrieveLoggedUser(String token) {

        return webClient.get()
                .uri(format(usersUri, "@me"))
                .headers(headers -> {
                    headers.add(AUTHORIZATION, format(BEARER, token));
                    headers.add(CONTENT_TYPE, APPLICATION_JSON_VALUE);
                })
                .retrieve()
                .onStatus(UNAUTHORIZED_PREDICATE, this::handleUnauthorized)
                .onStatus(BAD_REQUEST_PREDICATE, this::handleBadRequest)
                .onStatus(HttpStatusCode::isError, this::handleUnknownError)
                .bodyToMono(DiscordUserDataResponse.class);
    }

    @Override
    public Mono<Void> logout(String clientId, String clientSecret, String token, String tokenTypeHint) {

        DiscordTokenRevocationRequest request = DiscordTokenRevocationRequest.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .token(token)
                .tokenTypeHint(tokenTypeHint)
                .build();

        return postForAuthentication(tokenRevokeUri, request)
                .bodyToMono(Void.class);
    }

    private ResponseSpec postForAuthentication(String url, Object request) {

        final MultiValueMap<String, String> valueMap = new LinkedMultiValueMap<>();
        Map<String, String> fieldMap = objectMapper.convertValue(request, new TypeReference<Map<String, String>>() {
        });

        valueMap.setAll(fieldMap);
        return webClient.post()
                .uri(url)
                .headers(headers -> {
                    headers.add(CONTENT_TYPE, CONTENT_TYPE_VALUE);
                    headers.add(ACCEPT, CONTENT_TYPE_VALUE);
                })
                .body(BodyInserters.fromFormData(valueMap))
                .retrieve()
                .onStatus(UNAUTHORIZED_PREDICATE, this::handleUnauthorized)
                .onStatus(BAD_REQUEST_PREDICATE, this::handleBadRequest)
                .onStatus(HttpStatusCode::isError, this::handleUnknownError);
    }

    private Mono<? extends Throwable> handleUnauthorized(ClientResponse clientResponse) {

        return Mono.error(new AuthenticationFailedException(AUTHENTICATION_ERROR, AUTHENTICATION_ERROR));
    }

    private Mono<? extends Throwable> handleBadRequest(ClientResponse clientResponse) {

        return clientResponse.bodyToMono(DiscordErrorResponse.class)
                .map(resp -> {
                    LOG.error(BAD_REQUEST_ERROR + " -> {}", resp);
                    return new DiscordApiException(BAD_REQUEST, resp.getError(),
                            resp.getErrorDescription(),
                            format(BAD_REQUEST_ERROR, resp.getError(), resp.getErrorDescription()));
                });
    }

    private Mono<? extends Throwable> handleUnknownError(ClientResponse clientResponse) {

        return clientResponse.bodyToMono(DiscordErrorResponse.class)
                .map(resp -> {
                    LOG.error(UNKNOWN_ERROR + " -> {}", resp);
                    return new DiscordApiException(INTERNAL_SERVER_ERROR, resp.getError(),
                            resp.getErrorDescription(),
                            format(UNKNOWN_ERROR, resp.getError(), resp.getErrorDescription()));
                });
    }
}
