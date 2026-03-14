package me.moirai.storyengine.infrastructure.inbound.rest.mapper;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.application.usecase.adventure.request.CreateAdventure;
import me.moirai.storyengine.core.application.usecase.adventure.request.DeleteAdventure;
import me.moirai.storyengine.core.application.usecase.adventure.request.UpdateAdventure;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateAdventureRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateAdventureRequest;

@Component
public class AdventureRequestMapper {

    public CreateAdventure toCommand(CreateAdventureRequest request, String requesterId) {

        return CreateAdventure.builder()
                .name(request.getName())
                .worldId(request.getWorldId())
                .personaId(request.getPersonaId())
                .visibility(request.getVisibility())
                .aiModel(request.getAiModel())
                .moderation(request.getModeration())
                .maxTokenLimit(request.getMaxTokenLimit())
                .temperature(request.getTemperature())
                .frequencyPenalty(request.getFrequencyPenalty())
                .presencePenalty(request.getPresencePenalty())
                .stopSequences(request.getStopSequences())
                .logitBias(request.getLogitBias())
                .usersAllowedToWrite(request.getUsersAllowedToWrite())
                .usersAllowedToRead(request.getUsersAllowedToRead())
                .channelId(request.getChannelId())
                .gameMode(request.getGameMode())
                .requesterId(requesterId)
                .isMultiplayer(request.isMultiplayer())
                .authorsNote(request.getAuthorsNote())
                .nudge(request.getNudge())
                .remember(request.getRemember())
                .bump(request.getBump())
                .bumpFrequency(request.getBumpFrequency())
                .build();
    }

    public UpdateAdventure toCommand(UpdateAdventureRequest request, String worldId, String requesterId) {

        return UpdateAdventure.builder()
                .id(worldId)
                .name(request.getName())
                .worldId(request.getWorldId())
                .personaId(request.getPersonaId())
                .visibility(request.getVisibility())
                .aiModel(request.getAiModel())
                .moderation(request.getModeration())
                .maxTokenLimit(request.getMaxTokenLimit())
                .temperature(request.getTemperature())
                .frequencyPenalty(request.getFrequencyPenalty())
                .presencePenalty(request.getPresencePenalty())
                .stopSequencesToAdd(request.getStopSequencesToAdd())
                .stopSequencesToRemove(request.getStopSequencesToRemove())
                .logitBiasToAdd(request.getLogitBiasToAdd())
                .logitBiasToRemove(request.getLogitBiasToRemove())
                .usersAllowedToWriteToAdd(request.getUsersAllowedToWriteToAdd())
                .usersAllowedToWriteToRemove(request.getUsersAllowedToWriteToRemove())
                .usersAllowedToReadToAdd(request.getUsersAllowedToReadToAdd())
                .usersAllowedToReadToRemove(request.getUsersAllowedToReadToRemove())
                .channelId(request.getChannelId())
                .gameMode(request.getGameMode())
                .requesterId(requesterId)
                .isMultiplayer(request.isMultiplayer())
                .authorsNote(request.getAuthorsNote())
                .nudge(request.getNudge())
                .remember(request.getRemember())
                .bump(request.getBump())
                .bumpFrequency(request.getBumpFrequency())
                .adventureStart(request.getAdventureStart())
                .build();
    }

    public DeleteAdventure toCommand(String worldId, String requesterId) {

        return DeleteAdventure.build(worldId, requesterId);
    }
}
