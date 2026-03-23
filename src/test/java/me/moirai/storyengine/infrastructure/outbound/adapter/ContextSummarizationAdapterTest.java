package me.moirai.storyengine.infrastructure.outbound.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;

import me.moirai.storyengine.AbstractWebMockTest;
import me.moirai.storyengine.core.application.usecase.discord.DiscordMessageDataFixture;
import me.moirai.storyengine.core.port.outbound.discord.DiscordMessageData;
import me.moirai.storyengine.core.port.outbound.generation.ChatMessagePort;
import me.moirai.storyengine.core.port.outbound.generation.TokenizerPort;
import me.moirai.storyengine.common.dto.ChatMessage;
import me.moirai.storyengine.infrastructure.outbound.adapter.generation.CompletionResponse;
import me.moirai.storyengine.infrastructure.outbound.adapter.generation.CompletionResponseChoice;
import me.moirai.storyengine.infrastructure.outbound.adapter.generation.StorySummarizationAdapter;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.StoryGenerationRequestFixture;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class ContextSummarizationAdapterTest extends AbstractWebMockTest {

    @Mock
    private TokenizerPort tokenizerPort;

    @Mock
    private ChatMessagePort chatMessageService;

    private StorySummarizationAdapter service;

    @BeforeEach
    void before() {

        var restClient = RestClient.builder()
                .baseUrl("http://localhost:" + PORT)
                .build();

        service = new StorySummarizationAdapter("Generate summary of the story so far",
                "/completions", "test-token", tokenizerPort, chatMessageService,
                restClient, jsonMapper);
    }

    @Test
    public void summarizeWith_validInput_thenSummaryGenerated() throws JsonProcessingException {

        // Given
        var generatedSummary = "Generated summary";
        var storyGenerationRequest = StoryGenerationRequestFixture.create();

        var context = createContextWithMessageNumber(3);

        prepareWebserverFor(completionResponseWith(generatedSummary), 200);

        when(chatMessageService.addMessagesToContext(anyMap(), anyInt(), anyInt()))
                .thenReturn(context);
        when(chatMessageService.addMessagesToContext(anyMap(), anyInt(), anyString()))
                .thenReturn(context);

        // When
        var result = service.summarizeContextWith(context, storyGenerationRequest);

        // Then
        assertThat(result).containsKey("summary");
        assertThat(result.get("summary")).isEqualTo(generatedSummary);
    }

    @Test
    public void summarizeWith_emptyMessageHistory_thenEmptySummaryReturned() throws JsonProcessingException {

        // Given
        var storyGenerationRequest = StoryGenerationRequestFixture.create();

        var context = createContextWithMessageNumber(3);

        prepareWebserverFor(completionResponseWith(""), 200);

        when(chatMessageService.addMessagesToContext(anyMap(), anyInt(), anyInt()))
                .thenReturn(context);
        when(chatMessageService.addMessagesToContext(anyMap(), anyInt(), anyString()))
                .thenReturn(context);

        // When
        var result = service.summarizeContextWith(context, storyGenerationRequest);

        // Then
        assertThat(result).containsKey("summary");
        assertThat((String) result.get("summary")).isEmpty();
    }

    @Test
    public void summarizeWith_whenSummaryExceedsTokenLimit_thenSummaryShouldBeTrimmed() throws JsonProcessingException {

        // Given
        var longSummary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam egestas dignissim velit, ut pellentesque ipsum. Ut auctor ipsum suscipit sapien tristique suscipit. Donec bibendum lectus neque, nec porttitor turpis commodo at. Nulla facilisi. Nulla gravida interdum tempor. Mauris iaculis pharetra leo.";
        var trimmedSummary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam egestas dignissim velit, ut pellentesque ipsum. Ut auctor ipsum suscipit sapien tristique suscipit. Donec bibendum lectus neque, nec porttitor turpis commodo at. Nulla facilisi. Nulla gravida interdum tempor.";
        var storyGenerationRequest = StoryGenerationRequestFixture.create();

        var context = createContextWithMessageNumber(3);

        prepareWebserverFor(completionResponseWith(longSummary), 200);

        when(tokenizerPort.getTokenCountFrom(not(eq(longSummary))))
                .thenReturn(1000)
                .thenReturn(200);

        when(tokenizerPort.getTokenCountFrom(contains("Message")))
                .thenReturn(20)
                .thenReturn(20)
                .thenReturn(1000)
                .thenReturn(20)
                .thenReturn(1000);

        when(tokenizerPort.getTokenCountFrom(longSummary))
                .thenReturn(200000);

        when(chatMessageService.addMessagesToContext(anyMap(), anyInt(), anyInt()))
                .thenReturn(context);
        when(chatMessageService.addMessagesToContext(anyMap(), anyInt(), anyString()))
                .thenReturn(context);

        // When
        var result = service.summarizeContextWith(context, storyGenerationRequest);

        // Then
        assertThat(result).containsKey("summary");
        assertThat(result).containsKey("messageHistory");

        var summary = (String) result.get("summary");
        assertThat(summary).isNotBlank().isEqualTo(trimmedSummary);

        var messageHistory = (List<String>) result.get("messageHistory");
        assertThat(messageHistory).isNotNull().isNotEmpty().hasSize(3);
    }

    @Test
    public void summarizeWith_whenSingleSentenceSummaryExceedsTokenLimit_thenSummaryShouldBeTrimmedToNothing() throws JsonProcessingException {

        // Given
        var longSummary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
        var storyGenerationRequest = StoryGenerationRequestFixture.create();

        var context = createContextWithMessageNumber(3);

        prepareWebserverFor(completionResponseWith(longSummary), 200);

        when(tokenizerPort.getTokenCountFrom(not(eq(longSummary))))
                .thenReturn(1000)
                .thenReturn(200);

        when(tokenizerPort.getTokenCountFrom(contains("Message")))
                .thenReturn(20)
                .thenReturn(20)
                .thenReturn(1000)
                .thenReturn(20)
                .thenReturn(1000);

        when(tokenizerPort.getTokenCountFrom(longSummary))
                .thenReturn(200000);

        when(chatMessageService.addMessagesToContext(anyMap(), anyInt(), anyInt()))
                .thenReturn(context);
        when(chatMessageService.addMessagesToContext(anyMap(), anyInt(), anyString()))
                .thenReturn(context);

        // When
        var result = service.summarizeContextWith(context, storyGenerationRequest);

        // Then
        assertThat(result).containsKey("summary");
        assertThat(result).containsKey("messageHistory");

        var summary = (String) result.get("summary");
        assertThat(summary).isBlank();

        var messageHistory = (List<String>) result.get("messageHistory");
        assertThat(messageHistory).isNotNull().isNotEmpty().hasSize(3);
    }

    @Test
    public void summarizeWith_whenSummaryNotExceedsTokenLimit_thenSummaryShouldNotBeTrimmed() throws JsonProcessingException {

        // Given
        var longSummary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam egestas dignissim velit, ut pellentesque ipsum. Ut auctor ipsum suscipit sapien tristique suscipit. Donec bibendum lectus neque, nec porttitor turpis commodo at. Nulla facilisi. Nulla gravida interdum tempor. Mauris iaculis pharetra leo.";
        var storyGenerationRequest = StoryGenerationRequestFixture.create();

        var context = createContextWithMessageNumber(3);

        prepareWebserverFor(completionResponseWith(longSummary), 200);

        when(tokenizerPort.getTokenCountFrom(not(eq(longSummary))))
                .thenReturn(1000)
                .thenReturn(200);

        when(tokenizerPort.getTokenCountFrom(contains("Message")))
                .thenReturn(20)
                .thenReturn(20)
                .thenReturn(1000)
                .thenReturn(20)
                .thenReturn(1000);

        when(tokenizerPort.getTokenCountFrom(longSummary))
                .thenReturn(200);

        when(chatMessageService.addMessagesToContext(anyMap(), anyInt(), anyInt()))
                .thenReturn(context);
        when(chatMessageService.addMessagesToContext(anyMap(), anyInt(), anyString()))
                .thenReturn(context);

        // When
        var result = service.summarizeContextWith(context, storyGenerationRequest);

        // Then
        assertThat(result).containsKey("summary");
        assertThat(result).containsKey("messageHistory");

        var summary = (String) result.get("summary");
        assertThat(summary).isNotBlank().isEqualTo(longSummary);

        var messageHistory = (List<String>) result.get("messageHistory");
        assertThat(messageHistory).isNotNull().isNotEmpty().hasSize(3);
        assertThat(messageHistory).first().isEqualTo("Message 1");
        assertThat(messageHistory).last().isEqualTo("Message 3");
    }

    private Map<String, Object> createContextWithMessageNumber(int items) {

        var messageDataList = new ArrayList<DiscordMessageData>();
        for (int i = 0; i < items; i++) {
            var messageNumber = i + 1;
            var base = DiscordMessageDataFixture.messageData();
            messageDataList.add(new DiscordMessageData(
                    String.valueOf(messageNumber), base.channelId(),
                    String.format("Message %s", messageNumber),
                    base.author(), base.mentionedUsers()));
        }

        var messageStringList = messageDataList.stream()
                .map(DiscordMessageData::content)
                .collect(Collectors.toCollection(ArrayList::new));

        var context = new HashMap<String, Object>();
        context.put("retrievedMessages", messageDataList);
        context.put("messageHistory", messageStringList);

        return context;
    }

    private CompletionResponse completionResponseWith(String text) {

        var choice = CompletionResponseChoice.builder()
                .index(0)
                .message(ChatMessage.asAssistant(text))
                .finishReason("stop")
                .build();

        return CompletionResponse.builder()
                .choices(List.of(choice))
                .build();
    }
}
