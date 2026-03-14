package me.moirai.storyengine.infrastructure.outbound.adapter.discord;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;

import me.moirai.storyengine.AbstractWebMockTest;
import me.moirai.storyengine.common.exception.AuthenticationFailedException;
import me.moirai.storyengine.common.exception.DiscordApiException;
import me.moirai.storyengine.infrastructure.inbound.rest.response.DiscordAuthResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.DiscordErrorResponse;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.DiscordAuthRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.DiscordTokenRevocationRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.RefreshSessionTokenRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.response.DiscordUserDataResponse;
import reactor.test.StepVerifier;

public class DiscordAuthenticationAdapterTest extends AbstractWebMockTest {

    private static final String DUMMY_VALUE = "DUMMY";

    private DiscordAuthenticationAdapter adapter;

    @BeforeEach
    void before() {

        adapter = new DiscordAuthenticationAdapter("http://localhost:" + PORT,
                "/users", "/token", "/token/revoke",
                WebClient.builder(), objectMapper);
    }

    @Test
    public void refreshToken() throws JsonProcessingException {

        // Given
        RefreshSessionTokenRequest request = RefreshSessionTokenRequest.builder()
                .clientId(DUMMY_VALUE)
                .clientSecret(DUMMY_VALUE)
                .grantType(DUMMY_VALUE)
                .build();

        DiscordAuthResponse response = DiscordAuthResponse.builder()
                .accessToken(DUMMY_VALUE)
                .expiresIn(424234L)
                .refreshToken(DUMMY_VALUE)
                .scope(DUMMY_VALUE)
                .tokenType(DUMMY_VALUE)
                .build();

        prepareWebserverFor(response, 200);

        // Then
        StepVerifier.create(adapter.refreshSessionToken(request))
                .assertNext(resp -> {
                    assertThat(resp).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    public void authenticateOnDiscord() throws JsonProcessingException {

        // Given
        DiscordAuthRequest request = DiscordAuthRequest.builder()
                .clientId(DUMMY_VALUE)
                .clientSecret(DUMMY_VALUE)
                .code(DUMMY_VALUE)
                .grantType(DUMMY_VALUE)
                .redirectUri(DUMMY_VALUE)
                .scope(DUMMY_VALUE)
                .build();

        DiscordAuthResponse response = DiscordAuthResponse.builder()
                .accessToken(DUMMY_VALUE)
                .expiresIn(424234L)
                .refreshToken(DUMMY_VALUE)
                .scope(DUMMY_VALUE)
                .tokenType(DUMMY_VALUE)
                .build();

        prepareWebserverFor(response, 200);

        // Then
        StepVerifier.create(adapter.authenticate(request))
                .assertNext(resp -> {
                    assertThat(resp).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    public void unauthorizedWhenAuthenticateOnDiscord() {

        // Given
        DiscordAuthRequest request = DiscordAuthRequest.builder()
                .clientId(DUMMY_VALUE)
                .clientSecret(DUMMY_VALUE)
                .code(DUMMY_VALUE)
                .grantType(DUMMY_VALUE)
                .redirectUri(DUMMY_VALUE)
                .scope(DUMMY_VALUE)
                .build();

        prepareWebserverFor(401);

        // Then
        StepVerifier.create(adapter.authenticate(request))
                .expectError(AuthenticationFailedException.class)
                .verify();
    }

    @Test
    public void badRequestWhenAuthenticateOnDiscord()
            throws JsonProcessingException {

        // Given
        DiscordAuthRequest request = DiscordAuthRequest.builder()
                .clientId(DUMMY_VALUE)
                .clientSecret(DUMMY_VALUE)
                .code(DUMMY_VALUE)
                .grantType(DUMMY_VALUE)
                .redirectUri(DUMMY_VALUE)
                .scope(DUMMY_VALUE)
                .build();

        DiscordErrorResponse errorResponse = DiscordErrorResponse.builder()
                .errorDescription(DUMMY_VALUE)
                .error(DUMMY_VALUE)
                .build();

        prepareWebserverFor(errorResponse, 400);

        // Then
        StepVerifier.create(adapter.authenticate(request))
                .verifyError(DiscordApiException.class);
    }

    @Test
    public void internalErrorWhenAuthenticateOnDiscord()
            throws JsonProcessingException {

        // Given
        DiscordAuthRequest request = DiscordAuthRequest.builder()
                .clientId(DUMMY_VALUE)
                .clientSecret(DUMMY_VALUE)
                .code(DUMMY_VALUE)
                .grantType(DUMMY_VALUE)
                .redirectUri(DUMMY_VALUE)
                .scope(DUMMY_VALUE)
                .build();

        DiscordErrorResponse errorResponse = DiscordErrorResponse.builder()
                .errorDescription(DUMMY_VALUE)
                .error(DUMMY_VALUE)
                .build();

        prepareWebserverFor(errorResponse, 500);

        // Then
        StepVerifier.create(adapter.authenticate(request))
                .verifyError(DiscordApiException.class);
    }

    @Test
    public void logoutOnDiscord() {

        // Given
        DiscordTokenRevocationRequest request = DiscordTokenRevocationRequest.builder()
                .clientId(DUMMY_VALUE)
                .clientSecret(DUMMY_VALUE)
                .token(DUMMY_VALUE)
                .tokenTypeHint(DUMMY_VALUE)
                .build();

        wireMockServer.stubFor(any(anyUrl())
                .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)));

        // Then
        StepVerifier.create(adapter.logout(request))
                .verifyComplete();
    }

    @Test
    public void unauthorizedWhenLogoutOnDiscord() {

        // Given
        DiscordTokenRevocationRequest request = DiscordTokenRevocationRequest.builder()
                .clientId(DUMMY_VALUE)
                .clientSecret(DUMMY_VALUE)
                .token(DUMMY_VALUE)
                .tokenTypeHint(DUMMY_VALUE)
                .build();

        prepareWebserverFor(401);

        // Then
        StepVerifier.create(adapter.logout(request))
                .expectError(AuthenticationFailedException.class)
                .verify();
    }

    @Test
    public void badRequestWhenLogoutOnDiscord()
            throws JsonProcessingException {

        // Given
        DiscordTokenRevocationRequest request = DiscordTokenRevocationRequest.builder()
                .clientId(DUMMY_VALUE)
                .clientSecret(DUMMY_VALUE)
                .token(DUMMY_VALUE)
                .tokenTypeHint(DUMMY_VALUE)
                .build();

        DiscordErrorResponse errorResponse = DiscordErrorResponse.builder()
                .errorDescription(DUMMY_VALUE)
                .error(DUMMY_VALUE)
                .build();

        prepareWebserverFor(errorResponse, 400);

        // Then
        StepVerifier.create(adapter.logout(request))
                .verifyError(DiscordApiException.class);
    }

    @Test
    public void internalErrorWhenLogoutOnDiscord()
            throws JsonProcessingException {

        // Given
        DiscordTokenRevocationRequest request = DiscordTokenRevocationRequest.builder()
                .clientId(DUMMY_VALUE)
                .clientSecret(DUMMY_VALUE)
                .token(DUMMY_VALUE)
                .tokenTypeHint(DUMMY_VALUE)
                .build();

        DiscordErrorResponse errorResponse = DiscordErrorResponse.builder()
                .errorDescription(DUMMY_VALUE)
                .error(DUMMY_VALUE)
                .build();

        prepareWebserverFor(errorResponse, 500);

        // Then
        StepVerifier.create(adapter.logout(request))
                .verifyError(DiscordApiException.class);
    }

    @Test
    public void getLoggedUser() throws JsonProcessingException {

        // Given
        String token = "TOKEN";

        DiscordUserDataResponse response = DiscordUserDataResponse.builder()
                .globalNickname("displayName")
                .username("username")
                .email("email@email.com")
                .build();

        prepareWebserverFor(response, 200);

        // Then
        StepVerifier.create(adapter.retrieveLoggedUser(token))
                .assertNext(resp -> {
                    assertThat(resp).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    public void unauthorizedWhenGetLoggedUser() {

        // Given
        String token = "TOKEN";

        prepareWebserverFor(401);

        // Then
        StepVerifier.create(adapter.retrieveLoggedUser(token))
                .expectError(AuthenticationFailedException.class)
                .verify();
    }

    @Test
    public void badRequestWhenGetLoggedUser() throws JsonProcessingException {

        // Given
        String token = "TOKEN";

        DiscordErrorResponse errorResponse = DiscordErrorResponse.builder()
                .errorDescription(DUMMY_VALUE)
                .error(DUMMY_VALUE)
                .build();

        prepareWebserverFor(errorResponse, 400);

        // Then
        StepVerifier.create(adapter.retrieveLoggedUser(token))
                .verifyError(DiscordApiException.class);
    }

    @Test
    public void internalErrorWhenGetLoggedUser() throws JsonProcessingException {

        // Given
        String token = "TOKEN";

        DiscordErrorResponse errorResponse = DiscordErrorResponse.builder()
                .errorDescription(DUMMY_VALUE)
                .error(DUMMY_VALUE)
                .build();

        prepareWebserverFor(errorResponse, 500);

        // Then
        StepVerifier.create(adapter.retrieveLoggedUser(token))
                .verifyError(DiscordApiException.class);
    }
}
