package me.moirai.storyengine.infrastructure.outbound.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;

import me.moirai.storyengine.AbstractWebMockTest;
import me.moirai.storyengine.infrastructure.outbound.adapter.generation.CompletionResponseError;
import me.moirai.storyengine.infrastructure.outbound.adapter.generation.ModerationResponse;
import me.moirai.storyengine.infrastructure.outbound.adapter.generation.ModerationResult;
import me.moirai.storyengine.infrastructure.outbound.adapter.generation.TextModerationAdapter;
import reactor.test.StepVerifier;

public class TextModerationAdapterTest extends AbstractWebMockTest {

    private TextModerationAdapter adapter;

    @BeforeEach
    void before() {

        adapter = new TextModerationAdapter("http://localhost:" + PORT,
                "/moderation", "api-token", WebClient.builder());
    }

    @Test
    public void textModeration_whenValidRequest_thenOutputIsGenerated() throws JsonProcessingException {

        // Given
        String input = "This is the input";

        ModerationResponse expectedResponse = ModerationResponse.builder()
                .model("gpt-3.5")
                .id("id123")
                .results(Collections.singletonList(ModerationResult.builder()
                        .flagged(false)
                        .categoryScores(Collections.singletonMap("topic", "0.7"))
                        .categories(Collections.singletonMap("topic", true))
                        .build()))
                .build();

        prepareWebserverFor(expectedResponse, 200);

        // Then
        StepVerifier.create(adapter.moderate(input))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getModerationScores())
                            .containsAllEntriesOf(Collections.singletonMap("topic", 0.7));
                })
                .verifyComplete();
    }

    @Test
    public void textGeneration_whenBadRequestOnOpenAiApi_thenHandleException() throws JsonProcessingException {

        // Given
        String input = "This is the input";

        CompletionResponseError errorResponse = CompletionResponseError.builder()
                .message("There was an unknown error")
                .param("Parameter")
                .type("Type")
                .code("CODE")
                .build();

        prepareWebserverFor(errorResponse, BAD_REQUEST);

        // Then
        StepVerifier.create(adapter.moderate(input))
                .verifyErrorMessage("Bad request calling OpenAI Moderation API");
    }

    @Test
    public void textGeneration_whenInternalErrorOnOpenAiApi_thenHandleException() throws JsonProcessingException {

        // Given
        String input = "This is the input";

        CompletionResponseError errorResponse = CompletionResponseError.builder()
                .message("There was an unknown error")
                .param("Parameter")
                .type("Type")
                .code("CODE")
                .build();

        prepareWebserverFor(errorResponse, INTERNAL_SERVER_ERROR);

        // Then
        StepVerifier.create(adapter.moderate(input))
                .verifyErrorMessage("Error on OpenAI Moderation API");
    }

    @Test
    public void textGeneration_whenUnauthorizedOpenAiApi_thenHandleException() throws JsonProcessingException {

        // Given
        String input = "This is the input";

        CompletionResponseError errorResponse = CompletionResponseError.builder()
                .message("Bad request error")
                .param("Parameter")
                .type("Type")
                .code("CODE")
                .build();

        prepareWebserverFor(errorResponse, UNAUTHORIZED);

        // Then
        StepVerifier.create(adapter.moderate(input))
                .verifyErrorMessage("Error authenticating user on OpenAI");
    }
}
