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

        return AdventureRawDto.builder()
                .id(adventure.getId())
                .name(adventure.getName())
                .worldId(adventure.getWorldId())
                .personaId(adventure.getPersonaId())
                .channelId(adventure.getChannelId())
                .visibility(adventure.getVisibility().name())
                .aiModel(adventure.getModelConfiguration().getAiModel().toString())
                .moderation(adventure.getModeration().name())
                .maxTokenLimit(adventure.getModelConfiguration().getMaxTokenLimit())
                .temperature(adventure.getModelConfiguration().getTemperature())
                .frequencyPenalty(adventure.getModelConfiguration().getFrequencyPenalty())
                .presencePenalty(adventure.getModelConfiguration().getPresencePenalty())
                .stopSequences(adventure.getModelConfiguration().getStopSequences())
                .logitBias(adventure.getModelConfiguration().getLogitBias())
                .usersAllowedToWrite(adventure.getUsersAllowedToWrite())
                .usersAllowedToRead(adventure.getUsersAllowedToRead())
                .ownerId(adventure.getOwnerId())
                .creationDate(adventure.getCreationDate())
                .lastUpdateDate(adventure.getLastUpdateDate())
                .gameMode(adventure.getGameMode().name())
                .isMultiplayer(adventure.isMultiplayer())
                .description(adventure.getDescription())
                .adventureStart(adventure.getAdventureStart())
                .nudge(adventure.getContextAttributes().getNudge())
                .remember(adventure.getContextAttributes().getRemember())
                .authorsNote(adventure.getContextAttributes().getAuthorsNote())
                .bump(adventure.getContextAttributes().getBump())
                .bumpFrequency(adventure.getContextAttributes().getBumpFrequency())
                .build();
    }

    public SearchAdventuresResult mapToResult(Page<Adventure> pagedResult) {
        return SearchAdventuresResult.builder()
                .results(pagedResult.getContent()
                        .stream()
                        .map(this::mapToSearchResult)
                        .toList())
                .page(pagedResult.getNumber() + 1)
                .items(pagedResult.getNumberOfElements())
                .totalItems(pagedResult.getTotalElements())
                .totalPages(pagedResult.getTotalPages())
                .build();
    }

    private AdventureDetails mapToSearchResult(Adventure adventure) {

        return AdventureDetails.builder()
                .id(adventure.getId())
                .name(adventure.getName())
                .worldId(String.valueOf(adventure.getWorldId()))
                .channelId(adventure.getChannelId())
                .visibility(adventure.getVisibility().name())
                .aiModel(adventure.getModelConfiguration().getAiModel().toString())
                .moderation(adventure.getModeration().name())
                .maxTokenLimit(adventure.getModelConfiguration().getMaxTokenLimit())
                .temperature(adventure.getModelConfiguration().getTemperature())
                .frequencyPenalty(adventure.getModelConfiguration().getFrequencyPenalty())
                .presencePenalty(adventure.getModelConfiguration().getPresencePenalty())
                .stopSequences(adventure.getModelConfiguration().getStopSequences())
                .logitBias(adventure.getModelConfiguration().getLogitBias())
                .usersAllowedToWrite(adventure.getUsersAllowedToWrite())
                .usersAllowedToRead(adventure.getUsersAllowedToRead())
                .ownerId(adventure.getOwnerId())
                .creationDate(adventure.getCreationDate())
                .lastUpdateDate(adventure.getLastUpdateDate())
                .gameMode(adventure.getGameMode().name())
                .isMultiplayer(adventure.isMultiplayer())
                .description(adventure.getDescription())
                .adventureStart(adventure.getAdventureStart())
                .nudge(adventure.getContextAttributes().getNudge())
                .remember(adventure.getContextAttributes().getRemember())
                .authorsNote(adventure.getContextAttributes().getAuthorsNote())
                .bump(adventure.getContextAttributes().getBump())
                .bumpFrequency(adventure.getContextAttributes().getBumpFrequency())
                .build();
    }
}
