package me.moirai.storyengine.infrastructure.outbound.adapter.userdetails;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;

import me.moirai.storyengine.AbstractWebMockTest;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDataResponse;
import me.moirai.storyengine.infrastructure.outbound.adapter.discord.DiscordUserDetailsAdapter;

public class DiscordUserDetailsAdapterTest extends AbstractWebMockTest {

    private static final String BOT_TOKEN = "botToken";

    private DiscordUserDetailsAdapter adapter;

    @BeforeEach
    void before() {

        var restClient = RestClient.builder()
                .baseUrl("http://localhost:" + PORT)
                .build();

        adapter = new DiscordUserDetailsAdapter("/users/%s", BOT_TOKEN, restClient);
    }

    @Test
    void getUserById_whenUserIsFound_thenUserIsReturned() throws JsonProcessingException {

        // Given
        var userId = "USRID";
        var response = new DiscordUserDataResponse(userId, "username", null, null, "email@email.com", null, null);

        prepareWebserverFor(response, 200);

        // When
        var result = adapter.getUserById(userId);

        // Then
        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result.get().id()).isEqualTo(userId);
    }

    @Test
    void getUserById_whenCalled_thenAuthenticatesAsTheBot() throws JsonProcessingException {

        // Given
        var userId = "USRID";
        var response = new DiscordUserDataResponse(userId, "username", null, null, "email@email.com", null, null);

        prepareWebserverFor(response, 200);

        // When
        adapter.getUserById(userId);

        // Then
        wireMockServer.verify(getRequestedFor(urlEqualTo("/users/" + userId))
                .withHeader(AUTHORIZATION, equalTo("Bot " + BOT_TOKEN)));
    }
}
