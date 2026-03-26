package me.moirai.storyengine.infrastructure.outbound.adapter.userdetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;

import me.moirai.storyengine.AbstractWebMockTest;
import me.moirai.storyengine.common.exception.OpenAiApiException;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDataResponse;
import me.moirai.storyengine.infrastructure.outbound.adapter.discord.DiscordUserDetailsAdapter;
import me.moirai.storyengine.infrastructure.outbound.adapter.generation.CompletionResponseError;

public class DiscordUserDetailsAdapterTest extends AbstractWebMockTest {

    private static final String DUMMY_VALUE = "DUMMY";

    private DiscordUserDetailsAdapter adapter;

    @BeforeEach
    void before() {

        var restClient = RestClient.builder()
                .baseUrl("http://localhost:" + PORT)
                .build();

        adapter = new DiscordUserDetailsAdapter("/users/%s", restClient, jsonMapper);
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

    @Test
    void getUserById_whenUnauthorized_thenThrowException() {

        // Given
        var userId = "USRID";
        var token = "TOKEN";

        prepareWebserverFor(401);

        // Then
        assertThatThrownBy(() -> adapter.getUserById(userId, token))
                .isInstanceOf(OpenAiApiException.class);
    }

    @Test
    void getUserById_whenBadRequest_thenThrowException() throws JsonProcessingException {

        // Given
        var userId = "USRID";
        var token = "TOKEN";
        var errorResponse = CompletionResponseError.builder()
                .message(DUMMY_VALUE)
                .type(DUMMY_VALUE)
                .code(DUMMY_VALUE)
                .param(DUMMY_VALUE)
                .build();

        prepareWebserverFor(errorResponse, 400);

        // Then
        assertThatThrownBy(() -> adapter.getUserById(userId, token))
                .isInstanceOf(OpenAiApiException.class);
    }

    @Test
    void getUserById_whenInternalError_thenThrowException() throws JsonProcessingException {

        // Given
        var userId = "USRID";
        var token = "TOKEN";
        var errorResponse = CompletionResponseError.builder()
                .message(DUMMY_VALUE)
                .type(DUMMY_VALUE)
                .code(DUMMY_VALUE)
                .param(DUMMY_VALUE)
                .build();

        prepareWebserverFor(errorResponse, 500);

        // Then
        assertThatThrownBy(() -> adapter.getUserById(userId, token))
                .isInstanceOf(OpenAiApiException.class);
    }
}
