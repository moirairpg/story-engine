package me.moirai.storyengine.infrastructure.outbound.adapter.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventuresResult;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRawDto;
import me.moirai.storyengine.core.domain.adventure.Adventure;

@Component
public class AdventurePersistenceMapper {

    public AdventureRawDto mapToResult(Adventure adventure) {

        return new AdventureRawDto(
                adventure.getPublicId(),
                adventure.getName(),
                adventure.getDescription(),
                adventure.getAdventureStart(),
                adventure.getWorldId(),
                adventure.getPersonaId(),
                adventure.getChannelId(),
                adventure.getVisibility().name(),
                adventure.getModelConfiguration().aiModel().toString(),
                adventure.getModeration().name(),
                adventure.getGameMode().name(),
                adventure.getOwnerId(),
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().remember(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency(),
                adventure.getModelConfiguration().maxTokenLimit(),
                adventure.getModelConfiguration().temperature(),
                adventure.getModelConfiguration().frequencyPenalty(),
                adventure.getModelConfiguration().presencePenalty(),
                adventure.isMultiplayer(),
                adventure.getCreationDate(),
                adventure.getLastUpdateDate(),
                adventure.getModelConfiguration().logitBias(),
                adventure.getModelConfiguration().stopSequences(),
                adventure.getUsersAllowedToRead(),
                adventure.getUsersAllowedToWrite());
    }

    public SearchAdventuresResult mapToResult(Page<Adventure> pagedResult) {
        return new SearchAdventuresResult(
                pagedResult.getNumber() + 1,
                pagedResult.getTotalPages(),
                pagedResult.getNumberOfElements(),
                pagedResult.getTotalElements(),
                pagedResult.getContent()
                        .stream()
                        .map(this::mapToSearchResult)
                        .toList());
    }

    private AdventureDetails mapToSearchResult(Adventure adventure) {

        return new AdventureDetails(
                adventure.getPublicId(),
                adventure.getName(),
                adventure.getDescription(),
                adventure.getAdventureStart(),
                null,
                null,
                adventure.getChannelId(),
                adventure.getVisibility().name(),
                adventure.getModelConfiguration().aiModel().toString(),
                adventure.getModeration().name(),
                adventure.getGameMode().name(),
                adventure.getOwnerId(),
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().remember(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency(),
                adventure.getModelConfiguration().maxTokenLimit(),
                adventure.getModelConfiguration().temperature(),
                adventure.getModelConfiguration().frequencyPenalty(),
                adventure.getModelConfiguration().presencePenalty(),
                adventure.isMultiplayer(),
                adventure.getCreationDate(),
                adventure.getLastUpdateDate(),
                adventure.getModelConfiguration().logitBias(),
                adventure.getModelConfiguration().stopSequences(),
                adventure.getUsersAllowedToRead(),
                adventure.getUsersAllowedToWrite());
    }
}
