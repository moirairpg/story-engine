package me.moirai.storyengine.core.application.usecase.discord.slashcommands;

import static me.moirai.storyengine.common.enums.Moderation.DISABLED;

import java.util.ArrayList;
import java.util.List;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.port.inbound.discord.DiscordMessageData;
import me.moirai.storyengine.core.port.inbound.discord.slashcommands.RetryCommand;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.discord.DiscordChannelPort;
import me.moirai.storyengine.core.port.outbound.generation.AiModelRequest;
import me.moirai.storyengine.core.port.outbound.generation.ModelConfigurationRequest;
import me.moirai.storyengine.core.port.outbound.generation.ModerationConfigurationRequest;
import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationPort;
import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationRequest;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;

@UseCaseHandler
public class RetryCommandHandler extends AbstractUseCaseHandler<RetryCommand, Void> {

    private static final String CHANNEL_HAS_NO_MESSAGES = "Channel has no messages";
    private static final String COMMAND_ONLY_WHEN_LAST_MESSAGE_BY_BOT = "This command can only be used if the last message in channel was sent by the bot.";
    private static final String PERSONA_NOT_FOUND = "Adventure has no persona linked to it";

    private final DiscordChannelPort discordChannelPort;
    private final StoryGenerationPort storyGenerationPort;
    private final AdventureRepository adventureRepository;
    private final PersonaRepository personaRepository;

    public RetryCommandHandler(
            DiscordChannelPort discordChannelPort,
            StoryGenerationPort storyGenerationPort,
            AdventureRepository adventureRepository,
            PersonaRepository personaRepository) {

        this.discordChannelPort = discordChannelPort;
        this.adventureRepository = adventureRepository;
        this.personaRepository = personaRepository;
        this.storyGenerationPort = storyGenerationPort;
    }

    @Override
    public Void execute(RetryCommand useCase) {

        var lastMessageSent = discordChannelPort.getLastMessageIn(useCase.getChannelId())
                .orElseThrow(() -> new IllegalStateException(CHANNEL_HAS_NO_MESSAGES));

        if (!lastMessageSent.getAuthor().getId().equals(useCase.getBotId())) {
            throw new IllegalStateException(COMMAND_ONLY_WHEN_LAST_MESSAGE_BY_BOT);
        }

        discordChannelPort.deleteMessageById(useCase.getChannelId(), lastMessageSent.getId());

        var adventure = adventureRepository.findByChannelId(useCase.getChannelId())
                .orElseThrow(() -> new AssetNotFoundException("Adventure not found"));

        if (useCase.getChannelId().equals(adventure.getChannelId())) {
            var generationRequest = buildGenerationRequest(useCase, adventure);
            storyGenerationPort.continueStory(generationRequest);
        }

        return null;
    }

    private StoryGenerationRequest buildGenerationRequest(RetryCommand useCase,
            Adventure adventure) {

        var persona = personaRepository.findById(adventure.getPersonaId())
                .orElseThrow(() -> new AssetNotFoundException(PERSONA_NOT_FOUND));

        var modelConfigurationRequest = ModelConfigurationRequest.builder()
                .frequencyPenalty(adventure.getModelConfiguration().frequencyPenalty())
                .presencePenalty(adventure.getModelConfiguration().presencePenalty())
                .logitBias(adventure.getModelConfiguration().logitBias())
                .maxTokenLimit(adventure.getModelConfiguration().maxTokenLimit())
                .stopSequences(adventure.getModelConfiguration().stopSequences())
                .temperature(adventure.getModelConfiguration().temperature())
                .aiModel(AiModelRequest
                        .build(adventure.getModelConfiguration().aiModel().toString(),
                                adventure.getModelConfiguration().aiModel().getOfficialModelName(),
                                adventure.getModelConfiguration().aiModel().getHardTokenLimit()))
                .build();

        var isModerationEnabled = !adventure.getModeration().equals(DISABLED);
        var moderation = ModerationConfigurationRequest
                .build(isModerationEnabled, adventure.getModeration().isAbsolute(),
                        adventure.getModeration().getThresholds());

        var messageHistory = getMessageHistory(useCase.getChannelId());

        return StoryGenerationRequest.builder()
                .botId(useCase.getBotId())
                .botNickname(useCase.getBotNickname())
                .botUsername(useCase.getBotUsername())
                .channelId(useCase.getChannelId())
                .guildId(useCase.getGuildId())
                .moderation(moderation)
                .modelConfiguration(modelConfigurationRequest)
                .personaId(persona.getPublicId())
                .adventureId(adventure.getPublicId())
                .messageHistory(messageHistory)
                .gameMode(adventure.getGameMode().name())
                .nudge(adventure.getContextAttributes().nudge())
                .authorsNote(adventure.getContextAttributes().authorsNote())
                .remember(adventure.getContextAttributes().remember())
                .bump(adventure.getContextAttributes().bump())
                .bumpFrequency(adventure.getContextAttributes().bumpFrequency())
                .build();
    }

    private List<DiscordMessageData> getMessageHistory(String channelId) {

        var lastMessageSent = discordChannelPort.getLastMessageIn(channelId)
                .orElseThrow(() -> new IllegalStateException(CHANNEL_HAS_NO_MESSAGES));

        var messageHistory = new ArrayList<>(discordChannelPort
                .retrieveEntireHistoryBefore(lastMessageSent.getId(), channelId));

        messageHistory.addFirst(lastMessageSent);

        return messageHistory;
    }
}