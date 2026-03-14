package me.moirai.storyengine.infrastructure.outbound.adapter;

import static me.moirai.storyengine.core.domain.adventure.ArtificialIntelligenceModel.GPT4_MINI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

import me.moirai.storyengine.core.application.helper.ChatMessageHelper;
import me.moirai.storyengine.core.port.outbound.TextGenerationRequest;
import me.moirai.storyengine.core.application.model.result.TextGenerationResultFixture;
import me.moirai.storyengine.core.port.inbound.discord.DiscordMessageData;
import me.moirai.storyengine.core.application.usecase.discord.DiscordMessageDataFixture;
import me.moirai.storyengine.core.port.outbound.TokenizerPort;
import me.moirai.storyengine.core.port.DiscordChannelPort;
import me.moirai.storyengine.core.port.TextCompletionPort;
import me.moirai.storyengine.core.port.outbound.AiModelRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.ModelConfigurationRequestFixture;
import me.moirai.storyengine.core.port.outbound.StoryGenerationRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.StoryGenerationRequestFixture;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class ContextSummarizationAdapterTest {

    @Mock
    private DiscordChannelPort discordChannelOperationsPort;

    @Mock
    private TextCompletionPort openAiPort;

    @Mock
    private TokenizerPort tokenizerPort;

    @Mock
    private ChatMessageHelper chatMessageService;

    private StorySummarizationAdapter service;

    @BeforeEach
    public void before() {

        final String summarizationInstruction = "Generate summary of the story so far";
        service = new StorySummarizationAdapter(summarizationInstruction, openAiPort, tokenizerPort, chatMessageService);
    }

    @Test
    public void summarizeWith_validInput_thenSummaryGenerated() {

        // Given
        String generatedSummary = "Generated summary";
        StoryGenerationRequest storyGenerationRequest = StoryGenerationRequestFixture.create()
                .modelConfiguration(ModelConfigurationRequestFixture.gpt4Mini().build())
                .build();

        Map<String, Object> context = createContextWithMessageNumber(3);

        when(openAiPort.generateTextFrom(any(TextGenerationRequest.class)))
                .thenReturn(Mono.just(TextGenerationResultFixture.create()
                        .outputText(generatedSummary)
                        .build()));

        when(chatMessageService.addMessagesToContext(anyMap(), anyInt(), anyInt()))
                .thenReturn(context);

        // When
        Mono<Map<String, Object>> result = service.summarizeContextWith(context, storyGenerationRequest);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(processedContext -> (processedContext.containsKey("summary")
                        && processedContext.get("summary").equals(generatedSummary)))
                .verifyComplete();
    }

    @Test
    public void summarizeWith_emptyMessageHistory_thenEmptySummaryReturned() {

        // Given
        StoryGenerationRequest storyGenerationRequest = StoryGenerationRequestFixture.create()
                .modelConfiguration(ModelConfigurationRequestFixture.gpt4Mini().build())
                .build();
        Map<String, Object> context = createContextWithMessageNumber(3);

        when(openAiPort.generateTextFrom(any(TextGenerationRequest.class)))
                .thenReturn(Mono.just(TextGenerationResultFixture.create()
                        .outputText("")
                        .build()));

        when(chatMessageService.addMessagesToContext(anyMap(), anyInt(), anyInt()))
                .thenReturn(context);

        // When
        Mono<Map<String, Object>> result = service.summarizeContextWith(context, storyGenerationRequest);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(processedContext -> processedContext.containsKey("summary")
                        && ((String) processedContext.get("summary")).isEmpty())
                .verifyComplete();
    }

    @Test
    public void summarizeWith_whenSummaryExceedsTokenLimit_thenSummaryShouldBeTrimmed() {

        // Given
        String longSummary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam egestas dignissim velit, ut pellentesque ipsum. Ut auctor ipsum suscipit sapien tristique suscipit. Donec bibendum lectus neque, nec porttitor turpis commodo at. Nulla facilisi. Nulla gravida interdum tempor. Mauris iaculis pharetra leo.";
        String trimmedSummary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam egestas dignissim velit, ut pellentesque ipsum. Ut auctor ipsum suscipit sapien tristique suscipit. Donec bibendum lectus neque, nec porttitor turpis commodo at. Nulla facilisi. Nulla gravida interdum tempor.";
        StoryGenerationRequest storyGenerationRequest = StoryGenerationRequestFixture.create()
                .modelConfiguration(ModelConfigurationRequestFixture.gpt4Mini()
                        .aiModel(AiModelRequest.build(
                                GPT4_MINI.toString(),
                                GPT4_MINI.getOfficialModelName(),
                                GPT4_MINI.getHardTokenLimit()))
                        .build())
                .build();

        Map<String, Object> context = createContextWithMessageNumber(3);

        when(openAiPort.generateTextFrom(any(TextGenerationRequest.class)))
                .thenReturn(Mono.just(TextGenerationResultFixture.create()
                        .outputText(longSummary)
                        .build()));

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

        // When
        Mono<Map<String, Object>> result = service.summarizeContextWith(context, storyGenerationRequest);

        // Then
        StepVerifier.create(result)
                .assertNext(processedContext -> {
                    assertThat(processedContext).containsKey("summary");
                    assertThat(processedContext).containsKey("messageHistory");

                    String summary = (String) processedContext.get("summary");
                    assertThat(summary).isNotBlank().isEqualTo(trimmedSummary);

                    List<String> messageHistory = (List<String>) processedContext.get("messageHistory");
                    assertThat(messageHistory).isNotNull().isNotEmpty().hasSize(3);
                })
                .verifyComplete();

        verify(openAiPort, times(1)).generateTextFrom(any(TextGenerationRequest.class));
    }

    @Test
    public void summarizeWith_whenSingleSentenceSummaryExceedsTokenLimit_thenSummaryShouldBeTrimmedToNothing() {

        // Given
        String longSummary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
        StoryGenerationRequest storyGenerationRequest = StoryGenerationRequestFixture.create()
                .modelConfiguration(ModelConfigurationRequestFixture.gpt4Mini()
                        .aiModel(AiModelRequest.build(
                                GPT4_MINI.toString(),
                                GPT4_MINI.getOfficialModelName(),
                                GPT4_MINI.getHardTokenLimit()))
                        .build())
                .build();

        Map<String, Object> context = createContextWithMessageNumber(3);

        when(openAiPort.generateTextFrom(any(TextGenerationRequest.class)))
                .thenReturn(Mono.just(TextGenerationResultFixture.create()
                        .outputText(longSummary)
                        .build()));

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

        // When
        Mono<Map<String, Object>> result = service.summarizeContextWith(context, storyGenerationRequest);

        // Then
        StepVerifier.create(result)
                .assertNext(processedContext -> {
                    assertThat(processedContext).containsKey("summary");
                    assertThat(processedContext).containsKey("messageHistory");

                    String summary = (String) processedContext.get("summary");
                    assertThat(summary).isBlank();

                    List<String> messageHistory = (List<String>) processedContext.get("messageHistory");
                    assertThat(messageHistory).isNotNull().isNotEmpty().hasSize(3);
                })
                .verifyComplete();

        verify(openAiPort, times(1)).generateTextFrom(any(TextGenerationRequest.class));
    }

    @Test
    public void summarizeWith_whenSummaryNotExceedsTokenLimit_thenSummaryShouldNotBeTrimmed() {

        // Given
        String longSummary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam egestas dignissim velit, ut pellentesque ipsum. Ut auctor ipsum suscipit sapien tristique suscipit. Donec bibendum lectus neque, nec porttitor turpis commodo at. Nulla facilisi. Nulla gravida interdum tempor. Mauris iaculis pharetra leo.";
        StoryGenerationRequest storyGenerationRequest = StoryGenerationRequestFixture.create()
                .modelConfiguration(ModelConfigurationRequestFixture.gpt4Mini()
                        .aiModel(AiModelRequest.build(
                                GPT4_MINI.toString(),
                                GPT4_MINI.getOfficialModelName(),
                                GPT4_MINI.getHardTokenLimit()))
                        .build())
                .build();

        Map<String, Object> context = createContextWithMessageNumber(3);

        when(openAiPort.generateTextFrom(any(TextGenerationRequest.class)))
                .thenReturn(Mono.just(TextGenerationResultFixture.create()
                        .outputText(longSummary)
                        .build()));

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

        // When
        Mono<Map<String, Object>> result = service.summarizeContextWith(context, storyGenerationRequest);

        // Then
        StepVerifier.create(result)
                .assertNext(processedContext -> {
                    assertThat(processedContext).containsKey("summary");
                    assertThat(processedContext).containsKey("messageHistory");

                    String summary = (String) processedContext.get("summary");
                    assertThat(summary).isNotBlank().isEqualTo(longSummary);

                    List<String> messageHistory = (List<String>) processedContext.get("messageHistory");
                    assertThat(messageHistory).isNotNull().isNotEmpty().hasSize(3);
                    assertThat(messageHistory).first().isEqualTo("Message 1");
                    assertThat(messageHistory).last().isEqualTo("Message 3");
                })
                .verifyComplete();

        verify(openAiPort, times(1)).generateTextFrom(any(TextGenerationRequest.class));
    }

    private Map<String, Object> createContextWithMessageNumber(int items) {

        List<DiscordMessageData> messageDataList = new ArrayList<>();
        for (int i = 0; i < items; i++) {
            int messageNumber = i + 1;
            messageDataList.add(DiscordMessageDataFixture.messageData()
                    .id(String.valueOf(messageNumber))
                    .content(String.format("Message %s", messageNumber))
                    .build());
        }

        List<String> messageStringList = messageDataList.stream()
                .map(DiscordMessageData::getContent)
                .collect(Collectors.toCollection(ArrayList::new));

        Map<String, Object> context = new HashMap<>();
        context.put("retrievedMessages", messageDataList);
        context.put("messageHistory", messageStringList);

        return context;
    }
}
