package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import static me.moirai.storyengine.common.util.DefaultStringProcessors.PERIOD;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.replaceTemplateWithValue;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.stripChatPrefix;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.stripTrailingFragment;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.trimParagraph;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.LF;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.moirai.storyengine.common.dto.ChatMessage;
import me.moirai.storyengine.common.exception.OpenAiApiException;
import me.moirai.storyengine.common.util.StringProcessor;
import me.moirai.storyengine.core.port.inbound.discord.DiscordMessageData;
import me.moirai.storyengine.core.port.outbound.generation.ChatMessagePort;
import me.moirai.storyengine.core.port.outbound.generation.ModelConfigurationRequest;
import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationRequest;
import me.moirai.storyengine.core.port.outbound.generation.StorySummarizationPort;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationRequest;
import me.moirai.storyengine.core.port.outbound.generation.TokenizerPort;

@Component
@SuppressWarnings("unchecked")
public class StorySummarizationAdapter implements StorySummarizationPort {

    private static final Logger LOG = LoggerFactory.getLogger(StorySummarizationAdapter.class);

    private static final String AUTHENTICATION_ERROR = "Error authenticating user on OpenAI";
    private static final String UNKNOWN_ERROR = "Error on OpenAI API";
    private static final String BAD_REQUEST_ERROR = "Bad request calling OpenAI API";

    private static final Predicate<HttpStatusCode> BAD_REQUEST = statusCode -> statusCode
            .isSameCodeAs(HttpStatusCode.valueOf(400));

    private static final Predicate<HttpStatusCode> UNAUTHORIZED = statusCode -> statusCode
            .isSameCodeAs(HttpStatusCode.valueOf(401));

    private static final String SUMMARY = "summary";
    private static final Object LOREBOOK = "lorebook";
    private static final String RETRIEVED_MESSAGES = "retrievedMessages";
    private static final String MESSAGE_HISTORY = "messageHistory";

    private final String token;
    private final String completionsUri;
    private final RestClient discordClient;
    private final ObjectMapper objectMapper;
    private final String summarizationInstriction;
    private final TokenizerPort tokenizerPort;
    private final ChatMessagePort chatMessageService;

    public StorySummarizationAdapter(
            @Value("${moirai.discord.bot.summarization-instruction}") String summarizationInstriction,
            @Value("${moirai.openai.api.completions-uri}") String completionsUri,
            @Value("${moirai.openai.api.token}") String token,
            TokenizerPort tokenizerPort,
            ChatMessagePort chatMessageService,
            RestClient discordClient,
            ObjectMapper objectMapper) {

        this.summarizationInstriction = summarizationInstriction;
        this.completionsUri = completionsUri;
        this.token = token;
        this.tokenizerPort = tokenizerPort;
        this.chatMessageService = chatMessageService;
        this.discordClient = discordClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> summarizeContextWith(
            Map<String, Object> context,
            StoryGenerationRequest storyGenerationRequest) {

        var summaryResponse = generateSummary(context, storyGenerationRequest);
        var processor = new StringProcessor();
        var summary = summaryResponse.getChoices().get(0).getMessage().content();

        processor.addRule(stripChatPrefix());
        processor.addRule(stripTrailingFragment());
        processor.addRule(replaceTemplateWithValue(EMPTY, LF));

        summary = processor.process(summary);

        var totalTokens = storyGenerationRequest.getModelConfiguration().getAiModel().getHardTokenLimit();
        var reservedTokensForStory = (int) Math.floor(totalTokens * 0.30);
        var rawMessageHistory = (List<DiscordMessageData>) context.get(RETRIEVED_MESSAGES);

        context.put(RETRIEVED_MESSAGES, rawMessageHistory);
        context.put(SUMMARY, summary.trim());

        context.putAll(chatMessageService.addMessagesToContext(context, reservedTokensForStory, 5));
        context.putAll(addSummaryToContext(context, reservedTokensForStory));
        context.putAll(chatMessageService.addMessagesToContext(context, reservedTokensForStory, SUMMARY));

        return context;
    }

    private CompletionResponse generateSummary(
            Map<String, Object> context,
            StoryGenerationRequest storyGenerationRequest) {

        var lorebook = (String) context.get(LOREBOOK);
        var textGenerationRequest = createSummarizationRequest(lorebook, storyGenerationRequest);

        return discordClient.post()
                .uri(completionsUri)
                .headers(headers -> {
                    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                    headers.add(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE);
                })
                .body(toRequest(textGenerationRequest))
                .retrieve()
                .onStatus(UNAUTHORIZED, this::handleUnauthorized)
                .onStatus(BAD_REQUEST, this::handleBadRequest)
                .onStatus(HttpStatusCode::isError, this::handleUnknownError)
                .body(CompletionResponse.class);
    }

    private CompletionRequest toRequest(TextGenerationRequest request) {

        return CompletionRequest.builder()
                .frequencyPenalty(request.getFrequencyPenalty())
                .presencePenalty(request.getPresencePenalty())
                .temperature(request.getTemperature())
                .logitBias(request.getLogitBias())
                .stop(request.getStopSequences())
                .maxTokens(request.getMaxTokens())
                .model(request.getModel())
                .messages(request.getMessages()
                        .stream()
                        .map(message -> new ChatMessage(message.role(), message.content()))
                        .toList())
                .build();
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
                .map(messageData -> ChatMessage.asUser(messageData.getContent()))
                .forEach(chatMessages::addFirst);

        if (StringUtils.isNotBlank(lorebook)) {
            chatMessages.addFirst(ChatMessage.asSystem(lorebook));
        }

        chatMessages.addFirst(ChatMessage.asSystem(summarizationInstriction));

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
                .map(ChatMessage::content)
                .collect(Collectors.joining(LF));
    }

    private void handleUnauthorized(HttpRequest request, ClientHttpResponse response) throws IOException {
        throw new OpenAiApiException(HttpStatus.UNAUTHORIZED, AUTHENTICATION_ERROR);
    }

    private void handleBadRequest(HttpRequest request, ClientHttpResponse response) throws IOException {

        var error = mapErrorResponse(response);
        LOG.error(BAD_REQUEST_ERROR + " -> {}", error);
        throw new OpenAiApiException(HttpStatus.BAD_REQUEST, error.getType(), error.getMessage(),
                String.format(BAD_REQUEST_ERROR, error.getType(), error.getMessage()));
    }

    private void handleUnknownError(HttpRequest request, ClientHttpResponse response) throws IOException {

        var error = mapErrorResponse(response);
        LOG.error(UNKNOWN_ERROR + " -> {}", error);
        throw new OpenAiApiException(HttpStatus.INTERNAL_SERVER_ERROR, error.getType(), error.getMessage(),
                String.format(UNKNOWN_ERROR, error.getType(), error.getMessage()));
    }

    private CompletionResponseError mapErrorResponse(ClientHttpResponse response) throws IOException {
        return objectMapper.readValue(response.getBody(), CompletionResponseError.class);
    }
}
