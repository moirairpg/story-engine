package me.moirai.storyengine.core.application.helper;

import static org.apache.commons.lang3.StringUtils.LF;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.ListUtils;

import me.moirai.storyengine.common.annotation.Helper;
import me.moirai.storyengine.core.port.inbound.discord.DiscordMessageData;
import me.moirai.storyengine.core.port.outbound.TokenizerPort;

@Helper
@SuppressWarnings("unchecked")
public class ChatMessageHelperImpl implements ChatMessageHelper {

    private static final String MESSAGE_HISTORY = "messageHistory";
    private static final String RETRIEVED_MESSAGES = "retrievedMessages";

    private final TokenizerPort tokenizerPort;

    public ChatMessageHelperImpl(TokenizerPort tokenizerPort) {
        this.tokenizerPort = tokenizerPort;
    }

    @Override
    public Map<String, Object> addMessagesToContext(Map<String, Object> context, int reservedTokens) {

        List<DiscordMessageData> retrievedMessages = (List<DiscordMessageData>) context.get(RETRIEVED_MESSAGES);
        List<String> messageHistory = ListUtils.defaultIfNull(
                (List<String>) context.get(MESSAGE_HISTORY), new ArrayList<>());

        retrievedMessages.stream()
                .takeWhile(messageData -> {
                    int tokensInMessage = tokenizerPort.getTokenCountFrom(messageData.getContent());
                    int tokensInContext = tokenizerPort.getTokenCountFrom(stringifyList(messageHistory));
                    int tokensLeftInContext = reservedTokens - tokensInContext;

                    return tokensInMessage <= tokensLeftInContext;
                })
                .map(DiscordMessageData::getContent)
                .forEach(messageHistory::addFirst);

        retrievedMessages.removeIf(messageData -> messageHistory.contains(messageData.getContent()));

        context.put(RETRIEVED_MESSAGES, retrievedMessages);
        context.put(MESSAGE_HISTORY, messageHistory);

        return context;
    }

    @Override
    public Map<String, Object> addMessagesToContext(Map<String, Object> context,
            int reservedTokens, int amountOfMessage) {

        List<DiscordMessageData> retrievedMessages = (List<DiscordMessageData>) context.get(RETRIEVED_MESSAGES);
        List<String> messageHistory = ListUtils.defaultIfNull(
                (List<String>) context.get(MESSAGE_HISTORY), new ArrayList<>());

        IntStream.range(0, retrievedMessages.size())
                .takeWhile(index -> index < amountOfMessage)
                .mapToObj(retrievedMessages::get)
                .takeWhile(messageData -> {
                    int tokensInMessage = tokenizerPort.getTokenCountFrom(messageData.getContent());
                    int tokensInContext = tokenizerPort.getTokenCountFrom(stringifyList(messageHistory));
                    int tokensLeftInContext = reservedTokens - tokensInContext;

                    return tokensInMessage <= tokensLeftInContext;
                })
                .map(DiscordMessageData::getContent)
                .forEach(messageHistory::addFirst);

        retrievedMessages.removeIf(messageData -> messageHistory.contains(messageData.getContent()));

        context.put(RETRIEVED_MESSAGES, retrievedMessages);
        context.put(MESSAGE_HISTORY, messageHistory);

        return context;
    }

    @Override
    public Map<String, Object> addMessagesToContext(Map<String, Object> context,
            int reservedTokens, String assetManipulated) {

        String stringifiedAsset = (String) context.get(assetManipulated);
        List<DiscordMessageData> retrievedMessages = (List<DiscordMessageData>) context.get(RETRIEVED_MESSAGES);
        List<String> messageHistory = ListUtils.defaultIfNull(
                (List<String>) context.get(MESSAGE_HISTORY), new ArrayList<>());

        int tokensInAsset = tokenizerPort.getTokenCountFrom(stringifiedAsset);

        retrievedMessages.stream()
                .takeWhile(messageData -> {
                    int tokensInMessage = tokenizerPort.getTokenCountFrom(messageData.getContent());
                    int tokensInContext = tokenizerPort.getTokenCountFrom(stringifyList(messageHistory))
                            + tokensInAsset;

                    int tokensLeftInContext = reservedTokens - tokensInContext;

                    return tokensInMessage <= tokensLeftInContext;
                })
                .map(DiscordMessageData::getContent)
                .forEach(messageHistory::addFirst);

        retrievedMessages.removeIf(messageData -> messageHistory.contains(messageData.getContent()));

        context.put(MESSAGE_HISTORY, messageHistory);
        context.put(RETRIEVED_MESSAGES, retrievedMessages);

        return context;
    }

    private String stringifyList(List<String> list) {

        return list.stream().collect(Collectors.joining(LF));
    }
}
