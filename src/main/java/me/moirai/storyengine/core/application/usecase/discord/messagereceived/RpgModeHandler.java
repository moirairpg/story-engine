package me.moirai.storyengine.core.application.usecase.discord.messagereceived;

import static me.moirai.storyengine.common.enums.GameMode.RPG;
import static me.moirai.storyengine.common.enums.Moderation.DISABLED;

import java.util.ArrayList;
import java.util.List;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.discord.DiscordMessageData;
import me.moirai.storyengine.core.port.inbound.discord.messagereceived.RpgModeRequest;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.port.outbound.generation.AiModelRequest;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.discord.DiscordChannelPort;
import me.moirai.storyengine.core.port.outbound.generation.ModelConfigurationRequest;
import me.moirai.storyengine.core.port.outbound.generation.ModerationConfigurationRequest;
import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationPort;
import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationRequest;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import reactor.core.publisher.Mono;

@UseCaseHandler
public class RpgModeHandler extends AbstractUseCaseHandler<RpgModeRequest, Mono<Void>> {

    private static final String CHANNEL_HAS_NO_MESSAGES = "Channel has no messages";
    private static final String PERSONA_NOT_FOUND = "Adventure has no persona linked to it";

    private final StoryGenerationPort storyGenerationPort;
    private final AdventureRepository adventureRepository;
    private final PersonaRepository personaRepository;
    private final DiscordChannelPort discordChannelPort;

    public RpgModeHandler(StoryGenerationPort storyGenerationPort,
            AdventureRepository adventureRepository,
            PersonaRepository personaRepository,
            DiscordChannelPort discordChannelPort) {

        this.adventureRepository = adventureRepository;
        this.personaRepository = personaRepository;
        this.storyGenerationPort = storyGenerationPort;
        this.discordChannelPort = discordChannelPort;
    }

    @Override
    public Mono<Void> execute(RpgModeRequest query) {

        return adventureRepository.findByChannelId(query.getChannelId())
                .filter(adventure -> adventure.getChannelId().equals(query.getChannelId()))
                .map(adventure -> {
                    StoryGenerationRequest generationRequest = buildGenerationRequest(query, adventure);

                    if (adventure.isMultiplayer()) {
                        return Mono.<Void>empty();
                    }

                    return storyGenerationPort.continueStory(generationRequest);
                })
                .orElseGet(Mono::empty);
    }

    private StoryGenerationRequest buildGenerationRequest(RpgModeRequest useCase, Adventure adventure) {

        Persona persona = personaRepository.findById(adventure.getPersonaId())
                .orElseThrow(() -> new AssetNotFoundException(PERSONA_NOT_FOUND));

        AiModelRequest aiModel = AiModelRequest
                .build(adventure.getModelConfiguration().aiModel().toString(),
                        adventure.getModelConfiguration().aiModel().getOfficialModelName(),
                        adventure.getModelConfiguration().aiModel().getHardTokenLimit());

        ModelConfigurationRequest modelConfigurationRequest = ModelConfigurationRequest.builder()
                .frequencyPenalty(adventure.getModelConfiguration().frequencyPenalty())
                .presencePenalty(adventure.getModelConfiguration().presencePenalty())
                .logitBias(adventure.getModelConfiguration().logitBias())
                .maxTokenLimit(adventure.getModelConfiguration().maxTokenLimit())
                .stopSequences(adventure.getModelConfiguration().stopSequences())
                .temperature(adventure.getModelConfiguration().temperature())
                .aiModel(aiModel)
                .build();

        boolean isModerationEnabled = !adventure.getModeration().equals(DISABLED);
        ModerationConfigurationRequest moderation = ModerationConfigurationRequest
                .build(isModerationEnabled, adventure.getModeration().isAbsolute(),
                        adventure.getModeration().getThresholds());

        List<DiscordMessageData> messageHistory = getMessageHistory(useCase.getChannelId());

        return StoryGenerationRequest.builder()
                .botNickname(useCase.getBotNickname())
                .botUsername(useCase.getBotUsername())
                .channelId(useCase.getChannelId())
                .guildId(useCase.getGuildId())
                .moderation(moderation)
                .modelConfiguration(modelConfigurationRequest)
                .personaId(persona.getPublicId())
                .adventureId(adventure.getPublicId())
                .messageHistory(messageHistory)
                .gameMode(RPG.name())
                .nudge(adventure.getContextAttributes().nudge())
                .authorsNote(adventure.getContextAttributes().authorsNote())
                .remember(adventure.getContextAttributes().remember())
                .bump(adventure.getContextAttributes().bump())
                .bumpFrequency(adventure.getContextAttributes().bumpFrequency())
                .build();
    }

    private List<DiscordMessageData> getMessageHistory(String channelId) {

        DiscordMessageData lastMessageSent = discordChannelPort.getLastMessageIn(channelId)
                .orElseThrow(() -> new IllegalStateException(CHANNEL_HAS_NO_MESSAGES));

        List<DiscordMessageData> messageHistory = new ArrayList<>(discordChannelPort
                .retrieveEntireHistoryBefore(lastMessageSent.getId(), channelId));

        messageHistory.addFirst(lastMessageSent);

        return messageHistory;
    }
}