package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;

import me.moirai.storyengine.AbstractWebMockTest;
import me.moirai.storyengine.common.exception.RestException;

class TextModerationAdapterTest extends AbstractWebMockTest {

    private TextModerationAdapter adapter;

    @BeforeEach
    void before() {

        var restClient = RestClient.builder()
                .baseUrl("http://localhost:" + PORT)
                .build();

        adapter = new TextModerationAdapter("/moderation", restClient, jsonMapper);
    }

    @Test
    void shouldReturnModerationResultWhenValidRequest() throws JsonProcessingException {

        var expectedResponse = ModerationResponse.builder()
                .model("gpt-3.5")
                .id("id123")
                .results(Collections.singletonList(ModerationResult.builder()
                        .flagged(false)
                        .categoryScores(Collections.singletonMap("topic", "0.7"))
                        .categories(Collections.singletonMap("topic", true))
                        .build()))
                .build();

        prepareWebserverFor(expectedResponse, 200);

        var result = adapter.moderate("This is the input");

        assertThat(result).isNotNull();
        assertThat(result.moderationScores())
                .containsAllEntriesOf(Collections.singletonMap("topic", 0.7));
    }

    @Test
    void shouldThrowExceptionWhenBadRequest() throws JsonProcessingException {

        var errorResponse = CompletionResponseError.builder()
                .message("There was an unknown error")
                .param("Parameter")
                .type("Type")
                .code("CODE")
                .build();

        prepareWebserverFor(errorResponse, BAD_REQUEST);

        assertThatThrownBy(() -> adapter.moderate("This is the input"))
                .isInstanceOf(RestException.class)
                .hasMessageContaining("Bad request calling OpenAI Moderation API");
    }

    @Test
    void shouldThrowExceptionWhenInternalServerError() throws JsonProcessingException {

        var errorResponse = CompletionResponseError.builder()
                .message("There was an unknown error")
                .param("Parameter")
                .type("Type")
                .code("CODE")
                .build();

        prepareWebserverFor(errorResponse, INTERNAL_SERVER_ERROR);

        assertThatThrownBy(() -> adapter.moderate("This is the input"))
                .isInstanceOf(RestException.class)
                .hasMessageContaining("Error on OpenAI Moderation API");
    }

    @Test
    void shouldThrowExceptionWhenUnauthorized() throws JsonProcessingException {

        var errorResponse = CompletionResponseError.builder()
                .message("Bad request error")
                .param("Parameter")
                .type("Type")
                .code("CODE")
                .build();

        prepareWebserverFor(errorResponse, UNAUTHORIZED);

        assertThatThrownBy(() -> adapter.moderate("This is the input"))
                .isInstanceOf(RestException.class)
                .hasMessageContaining("Error authenticating user on OpenAI");
    }
}
