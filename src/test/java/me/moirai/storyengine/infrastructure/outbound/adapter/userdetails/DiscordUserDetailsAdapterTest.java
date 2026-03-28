package me.moirai.storyengine.infrastructure.outbound.adapter.userdetails;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;

import me.moirai.storyengine.AbstractWebMockTest;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDataResponse;
import me.moirai.storyengine.infrastructure.outbound.adapter.discord.DiscordUserDetailsAdapter;

public class DiscordUserDetailsAdapterTest extends AbstractWebMockTest {

    private DiscordUserDetailsAdapter adapter;

    @BeforeEach
    void before() {

        var restClient = RestClient.builder()
                .baseUrl("http://localhost:" + PORT)
                .build();

        adapter = new DiscordUserDetailsAdapter("/users/%s", restClient);
    }

    @Test
    void getUserById_whenUserIsFound_thenUserIsReturned() throws JsonProcessingException {

        // Given
        var userId = "USRID";
        var token = "TOKEN";
        var response = new DiscordUserDataResponse(userId, "username", "displayName", null, "email@email.com", null);

        prepareWebserverFor(response, 200);

        // When
        var result = adapter.getUserById(userId, token);

        // Then
        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result.get().id()).isEqualTo(userId);
    }
}
