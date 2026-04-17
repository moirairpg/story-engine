package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;

import me.moirai.storyengine.AbstractWebMockTest;
import me.moirai.storyengine.core.port.outbound.generation.ChatMessage;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationRequest;

class TextCompletionAdapterTest extends AbstractWebMockTest {

    private TextCompletionAdapter adapter;

    @BeforeEach
    void before() {

        var restClient = RestClient.builder()
                .baseUrl("http://localhost:" + PORT)
                .build();

        adapter = new TextCompletionAdapter("/responses", "test-token", restClient);
    }

    @Test
    void shouldReturnGenerationResultWhenValidRequest() throws JsonProcessingException {

        // given
        var outputContent = new OpenAiResponsesApiOutputContent();
        outputContent.setText("Generated response text");

        var output = new OpenAiResponsesApiOutput();
        output.setContent(Collections.singletonList(outputContent));

        var usage = new OpenAiResponsesApiUsage();
        usage.setInputTokens(10);
        usage.setOutputTokens(20);
        usage.setTotalTokens(30);

        var expectedResponse = new OpenAiResponsesApiResponse();
        expectedResponse.setOutput(Collections.singletonList(output));
        expectedResponse.setUsage(usage);

        prepareWebserverFor(expectedResponse, 200);

        var request = new TextGenerationRequest(
                "gpt-4o-mini",
                "You are a helpful assistant",
                List.of(ChatMessage.asUser("Hello")),
                100,
                1.0);

        // when
        var result = adapter.generateTextFrom(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOutputText()).isEqualTo("Generated response text");
        assertThat(result.getPromptTokens()).isEqualTo(10);
        assertThat(result.getCompletionTokens()).isEqualTo(20);
        assertThat(result.getTotalTokens()).isEqualTo(30);
    }
}
