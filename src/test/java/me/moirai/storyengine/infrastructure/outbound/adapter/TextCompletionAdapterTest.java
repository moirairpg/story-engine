package me.moirai.storyengine.infrastructure.outbound.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;

import me.moirai.storyengine.AbstractWebMockTest;
import me.moirai.storyengine.core.port.outbound.TextGenerationRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.response.ChatMessage;
import me.moirai.storyengine.infrastructure.outbound.adapter.response.CompletionResponse;
import me.moirai.storyengine.infrastructure.outbound.adapter.response.CompletionResponseChoice;
import me.moirai.storyengine.infrastructure.outbound.adapter.response.CompletionResponseError;
import me.moirai.storyengine.infrastructure.outbound.adapter.response.CompletionResponseUsage;
import reactor.test.StepVerifier;

public class TextCompletionAdapterTest extends AbstractWebMockTest {

    private TextCompletionAdapter adapter;

    @BeforeEach
    void before() {

        adapter = new TextCompletionAdapter("http://localhost:" + PORT,
                "/completion", "api-token", WebClient.builder());
    }

    @Test
    public void textGeneration_whenValidRequest_thenOutputIsGenerated() throws JsonProcessingException {

        // Given
        TextGenerationRequest request = TextGenerationRequest.builder()
                .frequencyPenalty(1.0)
                .presencePenalty(1.0)
                .temperature(1.0)
                .maxTokens(100)
                .model("gpt-3.5")
                .logitBias(Collections.singletonMap("token", 1.0))
                .stopSequences(Collections.singleton("token"))
                .build();

        CompletionResponse expectedResponse = CompletionResponse.builder()
                .usage(CompletionResponseUsage.builder()
                        .completionTokens(100)
                        .promptTokens(100)
                        .totalTokens(100)
                        .build())
                .choices(Collections.singletonList(CompletionResponseChoice.builder()
                        .message(ChatMessage.builder()
                                .content("Text output")
                                .build())
                        .build()))
                .build();

        prepareWebserverFor(expectedResponse, OK);

        // Then
        StepVerifier.create(adapter.generateTextFrom(request))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getCompletionTokens()).isEqualTo(100);
                    assertThat(result.getPromptTokens()).isEqualTo(100);
                    assertThat(result.getTotalTokens()).isEqualTo(100);
                    assertThat(result.getOutputText()).isEqualTo("Text output");
                })
                .verifyComplete();
    }

    @Test
    public void textGeneration_whenBadRequestOnOpenAiApi_thenHandleException() throws JsonProcessingException {

        // Given
        TextGenerationRequest request = TextGenerationRequest.builder()
                .frequencyPenalty(1.0)
                .presencePenalty(1.0)
                .temperature(1.0)
                .maxTokens(100)
                .model("gpt-3.5")
                .logitBias(Collections.singletonMap("token", 1.0))
                .stopSequences(Collections.singleton("token"))
                .build();

        CompletionResponseError errorResponse = CompletionResponseError.builder()
                .message("There was an unknown error")
                .param("Parameter")
                .type("Type")
                .code("CODE")
                .build();

        prepareWebserverFor(errorResponse, BAD_REQUEST);

        // Then
        StepVerifier.create(adapter.generateTextFrom(request))
                .verifyErrorMessage("Bad request calling OpenAI API");
    }

    @Test
    public void textGeneration_whenInternalErrorOnOpenAiApi_thenHandleException() throws JsonProcessingException {

        // Given
        TextGenerationRequest request = TextGenerationRequest.builder()
                .frequencyPenalty(1.0)
                .presencePenalty(1.0)
                .temperature(1.0)
                .maxTokens(100)
                .model("gpt-3.5")
                .logitBias(Collections.singletonMap("token", 1.0))
                .stopSequences(Collections.singleton("token"))
                .build();

        CompletionResponseError errorResponse = CompletionResponseError.builder()
                .message("There was an unknown error")
                .param("Parameter")
                .type("Type")
                .code("CODE")
                .build();

        prepareWebserverFor(errorResponse, INTERNAL_SERVER_ERROR);

        // Then
        StepVerifier.create(adapter.generateTextFrom(request))
                .verifyErrorMessage("Error on OpenAI API");
    }

    @Test
    public void textGeneration_whenUnauthorizedOpenAiApi_thenHandleException() throws JsonProcessingException {

        // Given
        TextGenerationRequest request = TextGenerationRequest.builder()
                .frequencyPenalty(1.0)
                .presencePenalty(1.0)
                .temperature(1.0)
                .maxTokens(100)
                .model("gpt-3.5")
                .logitBias(Collections.singletonMap("token", 1.0))
                .stopSequences(Collections.singleton("token"))
                .build();

        CompletionResponseError errorResponse = CompletionResponseError.builder()
                .message("Bad request error")
                .param("Parameter")
                .type("Type")
                .code("CODE")
                .build();

        prepareWebserverFor(errorResponse, UNAUTHORIZED);

        // Then
        StepVerifier.create(adapter.generateTextFrom(request))
                .verifyErrorMessage("Error authenticating user on OpenAI");
    }
}
