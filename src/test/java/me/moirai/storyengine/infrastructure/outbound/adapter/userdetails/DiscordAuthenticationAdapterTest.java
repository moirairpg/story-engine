package me.moirai.storyengine.infrastructure.outbound.adapter.userdetails;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;

import me.moirai.storyengine.AbstractWebMockTest;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthRequest;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDataResponse;
import me.moirai.storyengine.core.port.outbound.discord.RefreshSessionTokenRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.discord.DiscordAuthenticationAdapter;

public class DiscordAuthenticationAdapterTest extends AbstractWebMockTest {

    private static final String DUMMY_VALUE = "DUMMY";

    private DiscordAuthenticationAdapter adapter;

    @BeforeEach
    void before() {

        var restClient = RestClient.builder()
                .baseUrl("http://localhost:" + PORT)
                .build();

        adapter = new DiscordAuthenticationAdapter("/users/%s", "/token", "/token/revoke",
                restClient, jsonMapper);
    }

    @Test
    public void refreshToken() throws JsonProcessingException {

        // Given
        var request = RefreshSessionTokenRequest.builder()
                .clientId(DUMMY_VALUE)
                .clientSecret(DUMMY_VALUE)
                .grantType(DUMMY_VALUE)
                .build();

        var response = new HashMap<String, Object>();
        response.put("access_token", DUMMY_VALUE);
        response.put("expires_in", 424234L);
        response.put("refresh_token", DUMMY_VALUE);
        response.put("scope", DUMMY_VALUE);
        response.put("token_type", DUMMY_VALUE);

        prepareWebserverFor(response, 200);

        // When
        var result = adapter.refreshSessionToken(request);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    public void authenticateOnDiscord() throws JsonProcessingException {

        // Given
        var request = DiscordAuthRequest.builder()
                .clientId(DUMMY_VALUE)
                .clientSecret(DUMMY_VALUE)
                .code(DUMMY_VALUE)
                .grantType(DUMMY_VALUE)
                .redirectUri(DUMMY_VALUE)
                .scope(DUMMY_VALUE)
                .build();

        var response = new HashMap<String, Object>();
        response.put("access_token", DUMMY_VALUE);
        response.put("expires_in", 424234L);
        response.put("refresh_token", DUMMY_VALUE);
        response.put("scope", DUMMY_VALUE);
        response.put("token_type", DUMMY_VALUE);

        prepareWebserverFor(response, 200);

        // When
        var result = adapter.authenticate(request);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    public void logoutOnDiscord() {

        wireMockServer.stubFor(any(anyUrl())
                .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)));

        // When/Then - no exception
        adapter.logout(DUMMY_VALUE, DUMMY_VALUE, DUMMY_VALUE, DUMMY_VALUE);
    }

    @Test
    public void getLoggedUser() throws JsonProcessingException {

        // Given
        var token = "TOKEN";

        var response = new DiscordUserDataResponse(
                null,
                "username",
                "displayName",
                null,
                "email@email.com",
                null);

        prepareWebserverFor(response, 200);

        // When
        var result = adapter.getLoggedUser(token);

        // Then
        assertThat(result).isNotNull();
    }
}
