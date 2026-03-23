package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import static me.moirai.storyengine.common.util.DefaultStringProcessors.formatRpgDirective;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.replaceTemplateWithValueIgnoreCase;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.LF;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.common.util.StringUtils;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.util.StringProcessor;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntry;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.discord.DiscordMessageData;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDetails;
import me.moirai.storyengine.core.port.outbound.generation.ChatMessagePort;
import me.moirai.storyengine.core.port.outbound.generation.LorebookEnrichmentPort;
import me.moirai.storyengine.core.port.outbound.generation.ModelConfigurationRequest;
import me.moirai.storyengine.core.port.outbound.generation.TokenizerPort;

@Component
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class LorebookEnrichmentAdapter implements LorebookEnrichmentPort {

    private static final String ENTRY_DESCRIPTION = "[ Description of %s: %s ]";
    private static final String RETRIEVED_MESSAGES = "retrievedMessages";
    private static final String LOREBOOK = "lorebook";
    private static final String ADVENTURE_TO_BE_VIEWED_WAS_NOT_FOUND = "Adventure to be viewed was not found";
    private static final String LOREBOOK_ENTRY_TO_BE_VIEWED_NOT_FOUND = "Lorebook entry to be viewed was not found";

    private final TokenizerPort tokenizerPort;
    private final AdventureRepository adventureRepository;
    private final ChatMessagePort chatMessageService;

    public LorebookEnrichmentAdapter(
            TokenizerPort tokenizerPort,
            AdventureRepository adventureRepository,
            ChatMessagePort chatMessageService) {

        this.tokenizerPort = tokenizerPort;
        this.adventureRepository = adventureRepository;
        this.chatMessageService = chatMessageService;
    }

    @Override
    public Map<String, Object> enrichContextWithLorebookForRpg(
            List<DiscordMessageData> rawMessageHistory,
            UUID adventureId,
            ModelConfigurationRequest modelConfiguration) {

        int totalTokens = modelConfiguration.aiModel().hardTokenLimit();
        int reservedTokensForLorebook = (int) Math.floor(totalTokens * 0.30);

        List<AdventureLorebookEntry> entriesFound = findLorebookEntries(adventureId, rawMessageHistory);
        List<DiscordMessageData> formattedHistory = enrichMessagesWithLorebook(rawMessageHistory, entriesFound);
        List<String> formattedEntries = formatEntriesForContext(entriesFound, reservedTokensForLorebook);

        Map<String, Object> context = new HashMap<>();
        context.put(RETRIEVED_MESSAGES, new ArrayList<>(formattedHistory));

        String stringifiedLorebook = stringifyList(formattedEntries);
        if (StringUtils.isNotBlank(stringifiedLorebook)) {
            context.put(LOREBOOK, stringifiedLorebook);
        }

        return chatMessageService.addMessagesToContext(context, reservedTokensForLorebook);
    }

    @Override
    public Map<String, Object> enrichContextWithLorebook(
            List<DiscordMessageData> rawMessageHistory,
            UUID adventureId,
            ModelConfigurationRequest modelConfiguration) {

        int totalTokens = modelConfiguration.aiModel().hardTokenLimit();
        int reservedTokensForLorebook = (int) Math.floor(totalTokens * 0.30);

        List<String> messageHistory = rawMessageHistory.stream()
                .map(DiscordMessageData::content)
                .collect(Collectors.toCollection(ArrayList::new));

        String stringifiedStory = stringifyList(messageHistory);

        Map<String, Object> context = new HashMap<>();
        context.put(RETRIEVED_MESSAGES, new ArrayList<>(rawMessageHistory));

        Adventure adventure = adventureRepository.findByPublicId(adventureId)
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_TO_BE_VIEWED_WAS_NOT_FOUND));

        List<AdventureLorebookEntry> entriesFound = adventure.getLorebookEntriesByRegex(stringifiedStory);
        Map<String, Object> enrichedContext = addEntriesFoundToContext(entriesFound, context,
                reservedTokensForLorebook);

        return chatMessageService.addMessagesToContext(enrichedContext, reservedTokensForLorebook);
    }

    private List<AdventureLorebookEntry> findLorebookEntries(
            UUID adventureId,
            List<DiscordMessageData> rawMessageHistory) {

        List<AdventureLorebookEntry> entriesInHistory = findLorebookEntriesInHistory(adventureId, rawMessageHistory);
        List<AdventureLorebookEntry> entriesByMention = findLorebookEntriesByMention(adventureId, rawMessageHistory);
        List<AdventureLorebookEntry> entriesByAuthor = findLorebookEntriesByAuthor(adventureId, rawMessageHistory);

        Set<UUID> entryIdsNotDuplicated = new HashSet<>();
        return Stream.of(entriesInHistory, entriesByMention, entriesByAuthor)
                .flatMap(Collection::stream)
                .filter(entry -> entry.getPublicId() != null && entryIdsNotDuplicated.add(entry.getPublicId()))
                .toList();
    }

    private List<AdventureLorebookEntry> findLorebookEntriesInHistory(
            UUID adventureId,
            List<DiscordMessageData> rawMessageHistory) {

        List<String> messageHistory = rawMessageHistory.stream()
                .map(DiscordMessageData::content)
                .toList();

        Adventure adventure = adventureRepository.findByPublicId(adventureId)
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_TO_BE_VIEWED_WAS_NOT_FOUND));

        return adventure.getLorebookEntriesByRegex(stringifyList(messageHistory));
    }

    private List<AdventureLorebookEntry> findLorebookEntriesByMention(
            UUID adventureId,
            List<DiscordMessageData> rawMessageHistory) {

        return rawMessageHistory.stream()
                .flatMap(message -> message.mentionedUsers().stream())
                .map(user -> findLorebookEntryByPlayerDiscordId(user.getId(), adventureId))
                .toList();
    }

    private List<AdventureLorebookEntry> findLorebookEntriesByAuthor(
            UUID adventureId,
            List<DiscordMessageData> rawMessageHistory) {

        return rawMessageHistory.stream()
                .map(message -> {
                    try {
                        return findLorebookEntryByPlayerDiscordId(message.author().getId(), adventureId);
                    } catch (AssetNotFoundException exception) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        AdventureLorebookEntry::getPublicId,
                        entry -> entry,
                        (existing, replacement) -> existing))
                .values()
                .stream()
                .toList();
    }

    private AdventureLorebookEntry findLorebookEntryByPlayerDiscordId(String playerId, UUID adventureId) {

        Adventure adventure = adventureRepository.findByPublicId(adventureId)
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_TO_BE_VIEWED_WAS_NOT_FOUND));

        return adventure.getLorebookEntryByPlayerId(playerId)
                .orElseThrow(() -> new AssetNotFoundException(LOREBOOK_ENTRY_TO_BE_VIEWED_NOT_FOUND));
    }

    private List<DiscordMessageData> enrichMessagesWithLorebook(List<DiscordMessageData> rawMessageHistory,
            List<AdventureLorebookEntry> lorebook) {

        List<DiscordMessageData> messagesFormattedForMentions = formatMessagesWithMentions(
                rawMessageHistory, lorebook);

        return formatMessagesWithAuthor(messagesFormattedForMentions, lorebook);
    }

    private List<DiscordMessageData> formatMessagesWithMentions(
            List<DiscordMessageData> messageHistory, List<AdventureLorebookEntry> lorebook) {

        return messageHistory.stream()
                .flatMap(message -> {
                    List<DiscordMessageData> messages = new ArrayList<>();

                    if (isEmpty(message.mentionedUsers())) {
                        messages.add(message);
                    }

                    for (DiscordUserDetails mentionedUser : message.mentionedUsers()) {
                        messages.add(lorebook.stream()
                                .filter(entry -> mentionedUser.getId().equals(entry.getPlayerId()))
                                .findFirst()
                                .map(entry -> {
                                    StringProcessor processor = new StringProcessor();
                                    processor.addRule(replaceTemplateWithValueIgnoreCase(
                                            entry.getName(), mentionedUser.getNickname()));

                                    processor.addRule(replaceTemplateWithValueIgnoreCase(
                                            entry.getName(), mentionedUser.getMention()));

                                    processor.addRule(replaceTemplateWithValueIgnoreCase(
                                            entry.getName(), mentionedUser.getUsername()));

                                    return new DiscordMessageData(
                                            message.id(),
                                            null,
                                            processor.process(message.content()),
                                            message.author(),
                                            null);
                                })
                                .orElse(message));
                    }

                    return messages.stream();
                })
                .toList();
    }

    private List<DiscordMessageData> formatMessagesWithAuthor(
            List<DiscordMessageData> rawMessageHistory, List<AdventureLorebookEntry> lorebook) {

        return rawMessageHistory.stream()
                .map(message -> lorebook.stream()
                        .filter(entry -> message.author().getId().equals(entry.getPlayerId()))
                        .findFirst()
                        .map(entry -> {
                            DiscordUserDetails author = DiscordUserDetails.builder()
                                    .id(message.author().getId())
                                    .mention(message.author().getMention())
                                    .username(message.author().getUsername())
                                    .nickname(entry.getName())
                                    .build();

                            StringProcessor processor = new StringProcessor();
                            processor.addRule(replaceTemplateWithValueIgnoreCase(
                                    entry.getName(), message.author().getNickname()));

                            processor.addRule(replaceTemplateWithValueIgnoreCase(
                                    entry.getName(), message.author().getMention()));

                            processor.addRule(replaceTemplateWithValueIgnoreCase(
                                    entry.getName(), message.author().getUsername()));

                            String messageContent = processor.process(message.content());

                            return new DiscordMessageData(
                                    message.id(),
                                    null,
                                    formatRpgDirective(entry.getName()).apply(messageContent),
                                    author,
                                    null);
                        })
                        .orElse(message))
                .toList();
    }

    private List<String> formatEntriesForContext(List<AdventureLorebookEntry> entries, int reservedTokensForLorebook) {

        List<String> lorebook = new ArrayList<>();

        entries.stream()
                .takeWhile(entryData -> {
                    String entry = String.format(ENTRY_DESCRIPTION, entryData.getName(), entryData.getDescription());
                    String stringifiedLorebook = stringifyList(lorebook);

                    int tokensInEntry = tokenizerPort.getTokenCountFrom(entry);
                    int tokensInContext = tokenizerPort.getTokenCountFrom(stringifiedLorebook);

                    int tokensLeftInContext = reservedTokensForLorebook - tokensInContext;

                    return tokensInEntry <= tokensLeftInContext;
                })
                .map(entryData -> String.format(ENTRY_DESCRIPTION, entryData.getName(), entryData.getDescription()))
                .forEach(lorebook::add);

        return lorebook;
    }

    private Map<String, Object> addEntriesFoundToContext(List<AdventureLorebookEntry> entries,
            Map<String, Object> context,
            int reservedTokensForLorebook) {

        List<String> lorebook = new ArrayList<>();

        entries.stream()
                .takeWhile(entryData -> {
                    String entry = String.format(ENTRY_DESCRIPTION, entryData.getName(), entryData.getDescription());
                    String stringifiedLorebook = stringifyList(lorebook);

                    int tokensInEntry = tokenizerPort.getTokenCountFrom(entry);
                    int tokensInContext = tokenizerPort.getTokenCountFrom(stringifiedLorebook);

                    int tokensLeftInContext = reservedTokensForLorebook - tokensInContext;

                    return tokensInEntry <= tokensLeftInContext;
                })
                .map(entryData -> String.format(ENTRY_DESCRIPTION, entryData.getName(), entryData.getDescription()))
                .forEach(lorebook::add);

        String stringifiedLorebook = stringifyList(lorebook);
        if (StringUtils.isNotBlank(stringifiedLorebook)) {
            context.put(LOREBOOK, stringifiedLorebook);
        }

        return context;
    }

    private String stringifyList(List<String> list) {

        return list.stream().collect(Collectors.joining(LF));
    }
}
