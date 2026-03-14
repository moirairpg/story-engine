package me.moirai.storyengine.core.application.usecase.discord.slashcommands;

import static me.moirai.storyengine.core.domain.adventure.Moderation.DISABLED;

import java.util.ArrayList;
import java.util.List;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.application.helper.StoryGenerationHelper;
import me.moirai.storyengine.core.application.port.DiscordChannelPort;
import me.moirai.storyengine.core.application.usecase.discord.DiscordMessageData;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureRepository;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.AiModelRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.ModelConfigurationRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.ModerationConfigurationRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.StoryGenerationRequest;
import reactor.core.publisher.Mono;

@UseCaseHandler
public class RetryCommandHandler extends AbstractUseCaseHandler<RetryCommand, Mono<Void>> {

    private static final String CHANNEL_HAS_NO_MESSAGES = "Channel has no messages";
    private static final String COMMAND_ONLY_WHEN_LAST_MESSAGE_BY_BOT = "This command can only be used if the last message in channel was sent by the bot.";

    private final DiscordChannelPort discordChannelPort;
    private final StoryGenerationHelper storyGenerationPort;
    private final AdventureRepository adventureRepository;

    public RetryCommandHandler(DiscordChannelPort discordChannelPort,
            StoryGenerationHelper storyGenerationPort,
            AdventureRepository adventureRepository) {

        this.discordChannelPort = discordChannelPort;
        this.adventureRepository = adventureRepository;
        this.storyGenerationPort = storyGenerationPort;
    }

    @Override
    public Mono<Void> execute(RetryCommand useCase) {

        try {
            DiscordMessageData lastMessageSent = discordChannelPort.getLastMessageIn(useCase.getChannelId())
                    .orElseThrow(() -> new IllegalStateException(CHANNEL_HAS_NO_MESSAGES));

            if (!lastMessageSent.getAuthor().getId().equals(useCase.getBotId())) {
                return Mono.error(() -> new IllegalStateException(COMMAND_ONLY_WHEN_LAST_MESSAGE_BY_BOT));
            }

            discordChannelPort.deleteMessageById(useCase.getChannelId(), lastMessageSent.getId());

            return adventureRepository.findByChannelId(useCase.getChannelId())
                    .filter(adventure -> adventure.getChannelId().equals(useCase.getChannelId()))
                    .map(adventure -> buildGenerationRequest(useCase, adventure))
                    .map(storyGenerationPort::continueStory)
                    .orElseGet(Mono::empty);
        } catch (IllegalStateException e) {
            return Mono.error(e);
        } catch (Exception e) {
            return Mono.error(
                    () -> new IllegalStateException("An error occurred while retrying generation of output"));
        }
    }

    private StoryGenerationRequest buildGenerationRequest(RetryCommand useCase,
            Adventure adventure) {

        ModelConfigurationRequest modelConfigurationRequest = ModelConfigurationRequest.builder()
                .frequencyPenalty(adventure.getModelConfiguration().getFrequencyPenalty())
                .presencePenalty(adventure.getModelConfiguration().getPresencePenalty())
                .logitBias(adventure.getModelConfiguration().getLogitBias())
                .maxTokenLimit(adventure.getModelConfiguration().getMaxTokenLimit())
                .stopSequences(adventure.getModelConfiguration().getStopSequences())
                .temperature(adventure.getModelConfiguration().getTemperature())
                .aiModel(AiModelRequest
                        .build(adventure.getModelConfiguration().getAiModel().toString(),
                                adventure.getModelConfiguration().getAiModel().getOfficialModelName(),
                                adventure.getModelConfiguration().getAiModel().getHardTokenLimit()))
                .build();

        boolean isModerationEnabled = !adventure.getModeration().equals(DISABLED);
        ModerationConfigurationRequest moderation = ModerationConfigurationRequest
                .build(isModerationEnabled, adventure.getModeration().isAbsolute(),
                        adventure.getModeration().getThresholds());

        List<DiscordMessageData> messageHistory = getMessageHistory(useCase.getChannelId());

        return StoryGenerationRequest.builder()
                .botId(useCase.getBotId())
                .botNickname(useCase.getBotNickname())
                .botUsername(useCase.getBotUsername())
                .channelId(useCase.getChannelId())
                .guildId(useCase.getGuildId())
                .moderation(moderation)
                .modelConfiguration(modelConfigurationRequest)
                .personaId(adventure.getPersonaId())
                .adventureId(adventure.getId())
                .messageHistory(messageHistory)
                .gameMode(adventure.getGameMode().name())
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