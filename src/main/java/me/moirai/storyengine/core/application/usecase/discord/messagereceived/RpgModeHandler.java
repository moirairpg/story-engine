package me.moirai.storyengine.core.application.usecase.discord.messagereceived;

import static me.moirai.storyengine.core.domain.adventure.GameMode.RPG;
import static me.moirai.storyengine.core.domain.adventure.Moderation.DISABLED;

import java.util.ArrayList;
import java.util.List;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.application.helper.StoryGenerationHelper;
import me.moirai.storyengine.core.application.usecase.discord.DiscordMessageData;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.DiscordChannelPort;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.AiModelRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.ModelConfigurationRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.ModerationConfigurationRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.StoryGenerationRequest;
import reactor.core.publisher.Mono;

@UseCaseHandler
public class RpgModeHandler extends AbstractUseCaseHandler<RpgModeRequest, Mono<Void>> {

    private static final String CHANNEL_HAS_NO_MESSAGES = "Channel has no messages";

    private final StoryGenerationHelper storyGenerationPort;
    private final AdventureRepository adventureRepository;
    private final DiscordChannelPort discordChannelPort;

    public RpgModeHandler(StoryGenerationHelper storyGenerationPort,
            AdventureRepository adventureRepository,
            DiscordChannelPort discordChannelPort) {

        this.adventureRepository = adventureRepository;
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

        AiModelRequest aiModel = AiModelRequest
                .build(adventure.getModelConfiguration().getAiModel().toString(),
                        adventure.getModelConfiguration().getAiModel().getOfficialModelName(),
                        adventure.getModelConfiguration().getAiModel().getHardTokenLimit());

        ModelConfigurationRequest modelConfigurationRequest = ModelConfigurationRequest.builder()
                .frequencyPenalty(adventure.getModelConfiguration().getFrequencyPenalty())
                .presencePenalty(adventure.getModelConfiguration().getPresencePenalty())
                .logitBias(adventure.getModelConfiguration().getLogitBias())
                .maxTokenLimit(adventure.getModelConfiguration().getMaxTokenLimit())
                .stopSequences(adventure.getModelConfiguration().getStopSequences())
                .temperature(adventure.getModelConfiguration().getTemperature())
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
                .personaId(adventure.getPersonaId())
                .adventureId(adventure.getId())
                .messageHistory(messageHistory)
                .gameMode(RPG.name())
                .nudge(adventure.getContextAttributes().getNudge())
                .authorsNote(adventure.getContextAttributes().getAuthorsNote())
                .remember(adventure.getContextAttributes().getRemember())
                .bump(adventure.getContextAttributes().getBump())
                .bumpFrequency(adventure.getContextAttributes().getBumpFrequency())
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