package me.moirai.storyengine.infrastructure.outbound.adapter;

import static me.moirai.storyengine.common.util.DefaultStringProcessors.PERIOD;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.replaceTemplateWithValue;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.stripChatPrefix;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.stripTrailingFragment;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.trimParagraph;
import static me.moirai.storyengine.core.application.model.request.ChatMessage.Role.SYSTEM;
import static me.moirai.storyengine.core.application.model.request.ChatMessage.Role.USER;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.LF;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.util.StringProcessor;
import me.moirai.storyengine.core.application.helper.ChatMessageHelper;
import me.moirai.storyengine.core.application.model.request.ChatMessage;
import me.moirai.storyengine.core.application.model.request.TextGenerationRequest;
import me.moirai.storyengine.core.application.usecase.discord.DiscordMessageData;
import me.moirai.storyengine.core.domain.port.TokenizerPort;
import me.moirai.storyengine.core.port.StorySummarizationPort;
import me.moirai.storyengine.core.port.TextCompletionPort;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.ModelConfigurationRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.StoryGenerationRequest;
import reactor.core.publisher.Mono;

@Component
@SuppressWarnings("unchecked")
public class StorySummarizationAdapter implements StorySummarizationPort {

    private static final String SUMMARY = "summary";
    private static final Object LOREBOOK = "lorebook";
    private static final String RETRIEVED_MESSAGES = "retrievedMessages";
    private static final String MESSAGE_HISTORY = "messageHistory";

    private final String summarizationInstriction;
    private final TextCompletionPort openAiPort;
    private final TokenizerPort tokenizerPort;
    private final ChatMessageHelper chatMessageService;

    public StorySummarizationAdapter(
            @Value("${moirai.discord.bot.summarization-instruction}") String summarizationInstriction,
            TextCompletionPort openAiPort,
            TokenizerPort tokenizerPort,
            ChatMessageHelper chatMessageService) {

        this.summarizationInstriction = summarizationInstriction;
        this.openAiPort = openAiPort;
        this.tokenizerPort = tokenizerPort;
        this.chatMessageService = chatMessageService;
    }

    @Override
    public Mono<Map<String, Object>> summarizeContextWith(Map<String, Object> context,
            StoryGenerationRequest storyGenerationRequest) {

        int totalTokens = storyGenerationRequest.getModelConfiguration().getAiModel().getHardTokenLimit();
        int reservedTokensForStory = (int) Math.floor(totalTokens * 0.30);

        return generateSummary(context, storyGenerationRequest)
                .map(contextWithSummary -> {
                    contextWithSummary.putAll(
                            chatMessageService.addMessagesToContext(contextWithSummary, reservedTokensForStory, 5));

                    contextWithSummary.putAll(addSummaryToContext(contextWithSummary, reservedTokensForStory));

                    contextWithSummary.putAll(chatMessageService.addMessagesToContext(contextWithSummary,
                            reservedTokensForStory, SUMMARY));

                    return contextWithSummary;
                });
    }

    private Mono<? extends Map<String, Object>> generateSummary(Map<String, Object> context,
            StoryGenerationRequest storyGenerationRequest) {

        List<DiscordMessageData> rawMessageHistory = (List<DiscordMessageData>) context.get(RETRIEVED_MESSAGES);
        String lorebook = (String) context.get(LOREBOOK);

        TextGenerationRequest request = createSummarizationRequest(lorebook, storyGenerationRequest);
        return openAiPort.generateTextFrom(request)
                .map(summaryGenerated -> {
                    StringProcessor processor = new StringProcessor();
                    String summary = summaryGenerated.getOutputText().trim();

                    processor.addRule(stripChatPrefix());
                    processor.addRule(stripTrailingFragment());
                    processor.addRule(replaceTemplateWithValue(EMPTY, LF));

                    summary = processor.process(summary);

                    context.put(RETRIEVED_MESSAGES, rawMessageHistory);
                    context.put(SUMMARY, summary.trim());

                    return context;
                });
    }

    private Map<String, Object> addSummaryToContext(Map<String, Object> processedContext, int reservedTokensForStory) {

        String summary = (String) processedContext.get(SUMMARY);
        List<String> messageHistory = (List<String>) processedContext.get(MESSAGE_HISTORY);
        String messagesCollected = stringifyList(messageHistory);

        int tokensInSummary = tokenizerPort.getTokenCountFrom(summary);
        int tokensInContext = tokenizerPort.getTokenCountFrom(messagesCollected);
        int tokensLeftInContext = reservedTokensForStory - tokensInContext;

        while (tokensInSummary > tokensLeftInContext) {
            summary = trimParagraph().apply(summary);
            summary = summary.equals(PERIOD) ? EMPTY : summary;
            tokensInSummary = tokenizerPort.getTokenCountFrom(summary);
        }

        processedContext.put(SUMMARY, summary);

        return processedContext;
    }

    private TextGenerationRequest createSummarizationRequest(String lorebook,
            StoryGenerationRequest storyGenerationRequest) {

        ModelConfigurationRequest modelConfiguration = storyGenerationRequest.getModelConfiguration();
        List<ChatMessage> chatMessages = new ArrayList<>();

        storyGenerationRequest.getMessageHistory().stream()
                .takeWhile(message -> {
                    int tokensInMessage = tokenizerPort.getTokenCountFrom(message.getContent());
                    int tokensInRequest = tokenizerPort.getTokenCountFrom(stringifyMessageList(chatMessages));
                    int tokensAvailable = tokensInRequest - tokensInMessage;

                    return modelConfiguration.getAiModel()
                            .getHardTokenLimit() >= tokensAvailable;
                })
                .map(messageData -> ChatMessage.build(USER, messageData.getContent()))
                .forEach(chatMessages::addFirst);

        if (StringUtils.isNotBlank(lorebook)) {
            chatMessages.addFirst(ChatMessage.build(SYSTEM, lorebook));
        }

        chatMessages.addFirst(ChatMessage.build(SYSTEM, summarizationInstriction));

        return TextGenerationRequest.builder()
                .presencePenalty(modelConfiguration.getPresencePenalty())
                .frequencyPenalty(modelConfiguration.getFrequencyPenalty())
                .logitBias(modelConfiguration.getLogitBias())
                .maxTokens(modelConfiguration.getMaxTokenLimit())
                .model(modelConfiguration.getAiModel().getOfficialModelName())
                .stopSequences(modelConfiguration.getStopSequences())
                .temperature(modelConfiguration.getTemperature())
                .messages(chatMessages)
                .build();
    }

    private String stringifyList(List<String> list) {

        return list.stream().collect(Collectors.joining(LF));
    }

    private String stringifyMessageList(List<ChatMessage> list) {

        return list.stream()
                .map(ChatMessage::getContent)
                .collect(Collectors.joining(LF));
    }
}
