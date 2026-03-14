package me.moirai.storyengine.infrastructure.inbound.rest.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventureResult;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureResult;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventuresResult;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureResult;
import me.moirai.storyengine.infrastructure.inbound.rest.response.AdventureResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.CreateAdventureResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.SearchAdventuresResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.UpdateAdventureResponse;

@Component
public class AdventureResponseMapper {

    public SearchAdventuresResponse toResponse(SearchAdventuresResult result) {

        List<AdventureResponse> worlds = result.getResults()
                .stream()
                .map(this::toResponse)
                .toList();

        return SearchAdventuresResponse.builder()
                .page(result.getPage())
                .resultsInPage(result.getItems())
                .totalPages(result.getTotalPages())
                .totalResults(result.getTotalItems())
                .results(worlds)
                .build();
    }

    public AdventureResponse toResponse(GetAdventureResult result) {

        return AdventureResponse.builder()
                .id(result.getId())
                .name(result.getName())
                .worldId(result.getWorldId())
                .personaId(result.getPersonaId())
                .visibility(result.getVisibility())
                .aiModel(result.getAiModel())
                .moderation(result.getModeration())
                .maxTokenLimit(result.getMaxTokenLimit())
                .temperature(result.getTemperature())
                .frequencyPenalty(result.getFrequencyPenalty())
                .presencePenalty(result.getPresencePenalty())
                .stopSequences(result.getStopSequences())
                .logitBias(result.getLogitBias())
                .usersAllowedToWrite(result.getUsersAllowedToWrite())
                .usersAllowedToRead(result.getUsersAllowedToRead())
                .channelId(result.getChannelId())
                .gameMode(result.getGameMode())
                .isMultiplayer(result.isMultiplayer())
                .authorsNote(result.getAuthorsNote())
                .nudge(result.getNudge())
                .remember(result.getRemember())
                .bump(result.getBump())
                .bumpFrequency(result.getBumpFrequency())
                .ownerId(result.getOwnerId())
                .build();
    }

    public CreateAdventureResponse toResponse(CreateAdventureResult result) {

        return CreateAdventureResponse.build(result.getId());
    }

    public UpdateAdventureResponse toResponse(UpdateAdventureResult result) {

        return UpdateAdventureResponse.build(result.getLastUpdatedDateTime());
    }
}
