package me.moirai.storyengine.core.application.usecase.discord.slashcommands;

import static me.moirai.storyengine.common.enums.Moderation.DISABLED;

import java.util.List;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.port.inbound.discord.slashcommands.StartCommand;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.discord.DiscordChannelPort;
import me.moirai.storyengine.core.port.outbound.discord.DiscordMessageData;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDetails;
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

        var world = worldRepository.findById(adventure.getWorldId())
                .orElseThrow(() -> new AssetNotFoundException("Adventure has no world linked to it"));

        var persona = personaRepository.findById(adventure.getPersonaId())
                .orElseThrow(() -> new AssetNotFoundException("Adventure has no persona linked to it"));

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

        discordChannelPort.sendTextMessageTo(useCase.getChannelId(), world.getAdventureStart());

        var adventureStartMessage = new DiscordMessageData(
                null,
                useCase.getChannelId(),
                String.format(CHAT_FORMAT, useCase.getBotNickname(), world.getAdventureStart()),
                DiscordUserDetails.builder()
                        .id(useCase.getBotId())
                        .nickname(useCase.getBotNickname())
                        .username(useCase.getBotUsername())
                        .build(),
                List.of());

        return new StoryGenerationRequest(
                null,
                useCase.getBotUsername(),
                useCase.getBotNickname(),
                useCase.getChannelId(),
                useCase.getGuildId(),
                adventure.getPublicId(),
                persona.getPublicId(),
                adventure.getGameMode().name(),
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().remember(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency(),
                modelConfigurationRequest,
                moderation,
                List.of(adventureStartMessage));
    }
}
