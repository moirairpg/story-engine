package me.moirai.storyengine.core.application.usecase.discord.messagereceived;

import static java.util.Objects.nonNull;
import static me.moirai.storyengine.common.enums.GameMode.AUTHOR;
import static me.moirai.storyengine.common.enums.Moderation.DISABLED;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.formatAuthorDirective;
import static org.apache.commons.lang3.StringUtils.substringAfter;

import java.util.ArrayList;
import java.util.List;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.port.inbound.discord.messagereceived.AuthorModeRequest;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.discord.DiscordChannelPort;
import me.moirai.storyengine.core.port.outbound.discord.DiscordMessageData;
import me.moirai.storyengine.core.port.outbound.generation.AiModelRequest;
import me.moirai.storyengine.core.port.outbound.generation.ModelConfigurationRequest;
import me.moirai.storyengine.core.port.outbound.generation.ModerationConfigurationRequest;
import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationPort;
import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationRequest;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;

@UseCaseHandler
public class AuthorModeHandler extends AbstractUseCaseHandler<AuthorModeRequest, Void> {

    private static final String CHANNEL_HAS_NO_MESSAGES = "Channel has no messages";
    private static final String PERSONA_NOT_FOUND = "Adventure has no persona linked to it";

    private final StoryGenerationPort storyGenerationPort;
    private final AdventureRepository adventureRepository;
    private final PersonaRepository personaRepository;
    private final DiscordChannelPort discordChannelPort;

    public AuthorModeHandler(
            StoryGenerationPort storyGenerationPort,
            AdventureRepository adventureRepository,
            PersonaRepository personaRepository,
            DiscordChannelPort discordChannelPort) {

        this.adventureRepository = adventureRepository;
        this.personaRepository = personaRepository;
        this.storyGenerationPort = storyGenerationPort;
        this.discordChannelPort = discordChannelPort;
    }

    @Override
    public Void execute(AuthorModeRequest query) {

        var adventure = adventureRepository.findByChannelId(query.getChannelId())
                .orElseThrow(() -> new AssetNotFoundException("Adventure not found"));

        if (query.getChannelId().equals(adventure.getChannelId())) {
            var generationRequest = buildGenerationRequest(query, adventure);
            storyGenerationPort.continueStory(generationRequest);
        }

        return null;
    }

    private StoryGenerationRequest buildGenerationRequest(AuthorModeRequest useCase, Adventure adventure) {

        var persona = personaRepository.findById(adventure.getPersonaId())
                .orElseThrow(() -> new AssetNotFoundException(PERSONA_NOT_FOUND));

        var aiModel = new AiModelRequest(
                adventure.getModelConfiguration().aiModel().toString(),
                adventure.getModelConfiguration().aiModel().getOfficialModelName(),
                adventure.getModelConfiguration().aiModel().getHardTokenLimit());

        var modelConfigurationRequest = new ModelConfigurationRequest(
                aiModel,
                adventure.getModelConfiguration().maxTokenLimit(),
                adventure.getModelConfiguration().temperature(),
                adventure.getModelConfiguration().frequencyPenalty(),
                adventure.getModelConfiguration().presencePenalty(),
                adventure.getModelConfiguration().stopSequences(),
                adventure.getModelConfiguration().logitBias());

        var isModerationEnabled = !adventure.getModeration().equals(DISABLED);
        var moderation = new ModerationConfigurationRequest(
                isModerationEnabled,
                adventure.getModeration().isAbsolute(),
                adventure.getModeration().getThresholds());

        var messageHistory = getMessageHistory(useCase.getChannelId()).stream()
                .map(message -> formatHistoryForAuthorDirections(useCase, message))
                .toList();

        return new StoryGenerationRequest(
                null,
                useCase.getBotUsername(),
                useCase.getBotNickname(),
                useCase.getChannelId(),
                useCase.getGuildId(),
                adventure.getPublicId(),
                persona.getPublicId(),
                AUTHOR.name(),
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().remember(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency(),
                modelConfigurationRequest,
                moderation,
                messageHistory);
    }

    private List<DiscordMessageData> getMessageHistory(String channelId) {

        var lastMessageSent = discordChannelPort.getLastMessageIn(channelId)
                .orElseThrow(() -> new IllegalStateException(CHANNEL_HAS_NO_MESSAGES));

        var messageHistory = new ArrayList<>(discordChannelPort
                .retrieveEntireHistoryBefore(lastMessageSent.id(), channelId));

        messageHistory.addFirst(lastMessageSent);

        return messageHistory;
    }

    private DiscordMessageData formatHistoryForAuthorDirections(AuthorModeRequest useCase, DiscordMessageData message) {

        var botNickname = useCase.getBotUsername();
        var authorNickname = getAuthorNickname(message);

        if (!authorNickname.equals(botNickname)) {
            var originalMessageContent = substringAfter(message.content(),
                    String.format("%s said: ", authorNickname));

            var formattedMessageContent = formatAuthorDirective(authorNickname).apply(originalMessageContent);

            return new DiscordMessageData(
                    message.id(),
                    message.channelId(),
                    formattedMessageContent,
                    message.author(),
                    message.mentionedUsers());
        }

        return message;
    }

    private String getAuthorNickname(DiscordMessageData message) {
        return nonNull(message.author().getNickname()) ? message.author().getNickname()
                : message.author().getUsername();
    }
}