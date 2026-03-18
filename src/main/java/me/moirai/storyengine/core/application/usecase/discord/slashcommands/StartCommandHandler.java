package me.moirai.storyengine.core.application.usecase.discord.slashcommands;

import static me.moirai.storyengine.common.enums.Moderation.DISABLED;

import java.util.Collections;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.port.inbound.discord.DiscordMessageData;
import me.moirai.storyengine.core.port.inbound.discord.DiscordUserDetails;
import me.moirai.storyengine.core.port.inbound.discord.slashcommands.StartCommand;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.discord.DiscordChannelPort;
import me.moirai.storyengine.core.port.outbound.generation.AiModelRequest;
import me.moirai.storyengine.core.port.outbound.generation.ModelConfigurationRequest;
import me.moirai.storyengine.core.port.outbound.generation.ModerationConfigurationRequest;
import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationPort;
import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationRequest;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@UseCaseHandler
public class StartCommandHandler extends AbstractUseCaseHandler<StartCommand, Void> {

    private static final String CHAT_FORMAT = "%s said: %s";

    private final AdventureRepository adventureRepository;
    private final WorldRepository worldRepository;
    private final PersonaRepository personaRepository;
    private final StoryGenerationPort storyGenerationPort;
    private final DiscordChannelPort discordChannelPort;

    public StartCommandHandler(
            StoryGenerationPort storyGenerationPort,
            WorldRepository worldRepository,
            PersonaRepository personaRepository,
            AdventureRepository adventureRepository,
            DiscordChannelPort discordChannelPort) {

        this.adventureRepository = adventureRepository;
        this.worldRepository = worldRepository;
        this.personaRepository = personaRepository;
        this.storyGenerationPort = storyGenerationPort;
        this.discordChannelPort = discordChannelPort;
    }

    @Override
    public Void execute(StartCommand useCase) {

        var adventure = adventureRepository.findByChannelId(useCase.getChannelId())
                .orElseThrow(() -> new AssetNotFoundException("Adventure not found"));

        if (useCase.getChannelId().equals(adventure.getChannelId())) {
            var generationRequest = buildGenerationRequest(useCase, adventure);
            storyGenerationPort.continueStory(generationRequest);
        }

        return null;
    }

    private StoryGenerationRequest buildGenerationRequest(StartCommand useCase, Adventure adventure) {

        World world = worldRepository.findById(adventure.getWorldId())
                .orElseThrow(() -> new AssetNotFoundException("Adventure has no world linked to it"));

        Persona persona = personaRepository.findById(adventure.getPersonaId())
                .orElseThrow(() -> new AssetNotFoundException("Adventure has no persona linked to it"));

        ModelConfigurationRequest modelConfigurationRequest = ModelConfigurationRequest.builder()
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

        boolean isModerationEnabled = !adventure.getModeration().equals(DISABLED);
        ModerationConfigurationRequest moderation = ModerationConfigurationRequest
                .build(isModerationEnabled, adventure.getModeration().isAbsolute(),
                        adventure.getModeration().getThresholds());

        discordChannelPort.sendTextMessageTo(useCase.getChannelId(), world.getAdventureStart());

        DiscordMessageData adventureStartMessage = DiscordMessageData.builder()
                .channelId(useCase.getChannelId())
                .content(String.format(CHAT_FORMAT, useCase.getBotNickname(), world.getAdventureStart()))
                .mentionedUsers(Collections.emptyList())
                .author(DiscordUserDetails.builder()
                        .id(useCase.getBotId())
                        .nickname(useCase.getBotNickname())
                        .username(useCase.getBotUsername())
                        .build())
                .build();

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
                .messageHistory(Collections.singletonList(adventureStartMessage))
                .gameMode(adventure.getGameMode().name())
                .nudge(adventure.getContextAttributes().nudge())
                .authorsNote(adventure.getContextAttributes().authorsNote())
                .remember(adventure.getContextAttributes().remember())
                .bump(adventure.getContextAttributes().bump())
                .bumpFrequency(adventure.getContextAttributes().bumpFrequency())
                .build();
    }
}
