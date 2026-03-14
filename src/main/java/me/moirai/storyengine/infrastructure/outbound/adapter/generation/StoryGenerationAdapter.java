package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static me.moirai.storyengine.common.enums.AiRole.ASSISTANT;
import static me.moirai.storyengine.common.enums.AiRole.USER;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.PERIOD;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.SAID;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.formatAuthorsNote;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.formatBump;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.formatNudge;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.formatRemember;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.replaceTemplateWithValue;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.stripAsNamePrefix;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.stripAsNamePrefixForLowercase;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.stripChatPrefix;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.stripTrailingFragment;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.trimParagraph;
import static org.apache.commons.collections4.MapUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.dto.ChatMessage;
import me.moirai.storyengine.common.enums.AiRole;
import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.common.util.StringProcessor;
import me.moirai.storyengine.core.port.inbound.discord.DiscordMessageData;
import me.moirai.storyengine.core.port.outbound.discord.DiscordChannelPort;
import me.moirai.storyengine.core.port.outbound.generation.LorebookEnrichmentPort;
import me.moirai.storyengine.core.port.outbound.generation.ModerationConfigurationRequest;
import me.moirai.storyengine.core.port.outbound.generation.PersonaEnrichmentPort;
import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationPort;
import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationRequest;
import me.moirai.storyengine.core.port.outbound.generation.StorySummarizationPort;
import me.moirai.storyengine.core.port.outbound.generation.TextCompletionPort;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationRequest;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationResult;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;
import reactor.core.publisher.Mono;

@Component
@SuppressWarnings("unchecked")
public class StoryGenerationAdapter implements StoryGenerationPort {

    private static final String LOREBOOK_ENTRIES = "lorebook";
    private static final String STORY_SUMMARY = "summary";
    private static final String PERSONA = "persona";
    private static final String PERSONA_NAME = "personaName";
    private static final String MESSAGE_HISTORY = "messageHistory";
    private static final String RPG = "RPG";
    private static final int DISCORD_MAX_LENGTH = 2000;

    private final DiscordChannelPort discordChannelPort;
    private final StorySummarizationPort summarizationPort;
    private final LorebookEnrichmentPort lorebookEnrichmentPort;
    private final PersonaEnrichmentPort personaEnrichmentPort;
    private final TextCompletionPort textCompletionPort;
    private final TextModerationPort textModerationPort;

    public StoryGenerationAdapter(StorySummarizationPort summarizationPort,
            DiscordChannelPort discordChannelPort,
            LorebookEnrichmentPort lorebookEnrichmentPort,
            PersonaEnrichmentPort personaEnrichmentPort,
            TextCompletionPort textCompletionPort,
            TextModerationPort textModerationPort) {

        this.discordChannelPort = discordChannelPort;
        this.summarizationPort = summarizationPort;
        this.lorebookEnrichmentPort = lorebookEnrichmentPort;
        this.personaEnrichmentPort = personaEnrichmentPort;
        this.textCompletionPort = textCompletionPort;
        this.textModerationPort = textModerationPort;
    }

    @Override
    public Mono<Void> continueStory(StoryGenerationRequest request) {

        return Mono.just(request.getMessageHistory())
                .map(messageHistory -> enrichWithLorebook(request, messageHistory))
                .flatMap(contextWithLorebook -> summarizationPort.summarizeContextWith(contextWithLorebook, request))
                .flatMap(contextWithSummary -> personaEnrichmentPort.enrichContextWithPersona(
                        contextWithSummary, request.getPersonaId(), request.getModelConfiguration()))
                .map(contextWithPersona -> processEnrichedContext(contextWithPersona, request))
                .flatMap(processedContext -> moderateInput(processedContext, request.getModeration()))
                .flatMap(processedContext -> generateAiOutput(request, processedContext))
                .flatMap(aiOutput -> moderateOutput(aiOutput, request.getModeration()))
                .doOnNext(generatedOutput -> sendOutputTo(request.getChannelId(),
                        request.getBotUsername(), request.getBotNickname(), generatedOutput))
                .then();
    }

    private Map<String, Object> enrichWithLorebook(StoryGenerationRequest request,
            List<DiscordMessageData> messageHistory) {

        if (request.getGameMode().equals(RPG)) {
            return lorebookEnrichmentPort.enrichContextWithLorebookForRpg(messageHistory,
                    request.getAdventureId(), request.getModelConfiguration());
        }

        return lorebookEnrichmentPort.enrichContextWithLorebook(messageHistory,
                request.getAdventureId(), request.getModelConfiguration());
    }

    private Mono<TextGenerationResult> generateAiOutput(StoryGenerationRequest query,
            List<ChatMessage> processedContext) {
        TextGenerationRequest textGenerationRequest = buildTextGenerationRequest(query,
                processedContext);

        return textCompletionPort.generateTextFrom(textGenerationRequest);
    }

    private TextGenerationRequest buildTextGenerationRequest(StoryGenerationRequest query,
            List<ChatMessage> processedContext) {

        return TextGenerationRequest.builder()
                .frequencyPenalty(query.getModelConfiguration().getFrequencyPenalty())
                .presencePenalty(query.getModelConfiguration().getPresencePenalty())
                .temperature(query.getModelConfiguration().getTemperature())
                .model(query.getModelConfiguration().getAiModel().getOfficialModelName())
                .logitBias(query.getModelConfiguration().getLogitBias())
                .maxTokens(query.getModelConfiguration().getMaxTokenLimit())
                .stopSequences(query.getModelConfiguration().getStopSequences())
                .messages(processedContext)
                .build();
    }

    private List<ChatMessage> processEnrichedContext(Map<String, Object> unsortedContext,
            StoryGenerationRequest request) {

        List<ChatMessage> processedContext = new ArrayList<>();

        String persona = (String) unsortedContext.get(PERSONA);
        String personaName = (String) unsortedContext.get(PERSONA_NAME);
        String storySummary = (String) unsortedContext.get(STORY_SUMMARY);
        String lorebookEntries = (String) unsortedContext.get(LOREBOOK_ENTRIES);

        processedContext.add(ChatMessage.asSystem(
                replacePlaceholders(storySummary, request.getBotUsername(), request.getBotNickname(), personaName)));

        processedContext.addAll(buildContextForGeneration(unsortedContext,
                request.getBotUsername(), request.getBotNickname(), personaName));

        if (isNotBlank(lorebookEntries)) {
            processedContext.add(0, ChatMessage.asSystem(lorebookEntries));
        }

        processedContext.add(0, ChatMessage.asSystem(persona));

        handleNudge(request, processedContext);
        handleAuthorsNote(request, processedContext);
        handleRemember(request, processedContext);
        handleBump(request, processedContext);

        return processedContext;
    }

    private void handleBump(StoryGenerationRequest request, List<ChatMessage> processedContext) {

        int contextSize = processedContext.size();
        if (isNotBlank(request.getBump())) {
            for (int i = contextSize - 1 - request.getBumpFrequency(); i > 0; i = i - request.getBumpFrequency()) {
                String bump = formatBump().apply(request.getBump());
                processedContext.add(i, ChatMessage.asSystem(bump));
            }
        }
    }

    private void handleRemember(StoryGenerationRequest request, List<ChatMessage> processedContext) {

        if (isNotBlank(request.getRemember())) {
            String remember = formatRemember().apply(request.getRemember());
            processedContext.addFirst(ChatMessage.asSystem(remember));
        }
    }

    private void handleAuthorsNote(StoryGenerationRequest request, List<ChatMessage> processedContext) {

        if (isNotBlank(request.getAuthorsNote())) {
            String authorsNote = formatAuthorsNote().apply(request.getAuthorsNote());
            processedContext.add(ChatMessage.asSystem(authorsNote));
        }
    }

    private void handleNudge(StoryGenerationRequest request, List<ChatMessage> processedContext) {

        if (isNotBlank(request.getNudge())) {
            String nudge = formatNudge().apply(request.getNudge());
            processedContext.add(ChatMessage.asSystem(nudge));
        }
    }

    private String replacePlaceholders(String summary, String botName, String botNickname, String personaName) {

        StringProcessor processor = new StringProcessor();
        processor.addRule(stripChatPrefix());
        processor.addRule(stripTrailingFragment());
        processor.addRule(replaceTemplateWithValue(personaName, botName));
        processor.addRule(replaceTemplateWithValue(personaName, botNickname));

        return processor.process(summary);
    }

    private List<ChatMessage> buildContextForGeneration(Map<String, Object> unsortedContext,
            String botName, String botNickname, String personaName) {

        List<String> messageHistory = (List<String>) unsortedContext.get(MESSAGE_HISTORY);
        return messageHistory.stream()
                .map(message -> {
                    StringProcessor processor = new StringProcessor();
                    processor.addRule(replaceTemplateWithValue(personaName, botName));
                    processor.addRule(replaceTemplateWithValue(personaName, botNickname));

                    String modifiedContent = processor.process(message);
                    String senderName = message.substring(0, message.indexOf(SAID));
                    AiRole senderRole = senderName.equals(botName) ? ASSISTANT : USER;

                    return new ChatMessage(senderRole, modifiedContent);
                })
                .toList();
    }

    private void sendOutputTo(String messageChannelId, String botName, String botNickname, String content) {

        StringProcessor processor = new StringProcessor();
        processor.addRule(stripAsNamePrefixForLowercase(botName));
        processor.addRule(stripAsNamePrefix(botName));
        processor.addRule(stripAsNamePrefixForLowercase(botNickname));
        processor.addRule(stripAsNamePrefix(botNickname));
        processor.addRule(stripChatPrefix());
        processor.addRule(stripTrailingFragment());

        String output = processor.process(content);
        int outputSize = output.length();

        while (outputSize > DISCORD_MAX_LENGTH) {
            output = trimParagraph().apply(output);
            output = output.equals(PERIOD) ? EMPTY : output;
            outputSize = output.length();
        }

        discordChannelPort.sendTextMessageTo(messageChannelId, output);
    }

    private Mono<List<ChatMessage>> moderateInput(List<ChatMessage> messages,
            ModerationConfigurationRequest moderation) {

        String messageHistory = messages.stream()
                .map(ChatMessage::content)
                .collect(joining("\n"));

        return getTopicsFlaggedByModeration(messageHistory, moderation)
                .map(result -> {
                    if (!result.isEmpty()) {
                        throw new ModerationException("Inappropriate content found in message history", result);
                    }

                    return messages;
                });
    }

    private Mono<String> moderateOutput(TextGenerationResult generationResult,
            ModerationConfigurationRequest moderation) {

        return getTopicsFlaggedByModeration(generationResult.getOutputText(), moderation)
                .map(result -> {
                    if (!result.isEmpty()) {
                        throw new ModerationException("Inappropriate content found in AI's output", result);
                    }

                    return generationResult.getOutputText();
                });
    }

    private Mono<List<String>> getTopicsFlaggedByModeration(String input, ModerationConfigurationRequest moderation) {

        return textModerationPort.moderate(input)
                .map(result -> {
                    if (moderation.isAbsolute()) {
                        if (result.isContentFlagged()) {
                            return result.getFlaggedTopics();
                        }

                        return emptyList();
                    }

                    return result.getModerationScores()
                            .entrySet()
                            .stream()
                            .filter(entry -> isTopicFlagged(entry, moderation))
                            .map(Map.Entry::getKey)
                            .toList();
                });
    }

    private boolean isTopicFlagged(Entry<String, Double> entry, ModerationConfigurationRequest moderation) {

        if (isEmpty(moderation.getThresholds())) {
            return false;
        }

        return entry.getValue() > moderation.getThresholds().get(entry.getKey());
    }
}
