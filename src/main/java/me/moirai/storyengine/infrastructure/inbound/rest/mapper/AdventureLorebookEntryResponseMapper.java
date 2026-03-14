package me.moirai.storyengine.infrastructure.inbound.rest.mapper;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventureLorebookEntryResult;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureLorebookEntryResult;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntriesResult;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureLorebookEntryResult;
import me.moirai.storyengine.infrastructure.inbound.rest.response.CreateLorebookEntryResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.LorebookEntryResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.SearchLorebookEntriesResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.UpdateLorebookEntryResponse;

@Component
public class AdventureLorebookEntryResponseMapper {

    public SearchLorebookEntriesResponse toResponse(SearchAdventureLorebookEntriesResult result) {

        List<LorebookEntryResponse> lorebook = CollectionUtils.emptyIfNull(result.getResults())
                .stream()
                .map(this::toResponse)
                .toList();

        return SearchLorebookEntriesResponse.builder()
                .page(result.getPage())
                .resultsInPage(result.getItems())
                .totalPages(result.getTotalPages())
                .totalResults(result.getTotalItems())
                .results(lorebook)
                .build();
    }

    public LorebookEntryResponse toResponse(GetAdventureLorebookEntryResult result) {

        return LorebookEntryResponse.builder()
                .id(result.getId())
                .name(result.getName())
                .regex(result.getRegex())
                .description(result.getDescription())
                .playerId(result.getPlayerId())
                .isPlayerCharacter(result.isPlayerCharacter())
                .creationDate(result.getCreationDate())
                .lastUpdateDate(result.getLastUpdateDate())
                .build();
    }

    public CreateLorebookEntryResponse toResponse(CreateAdventureLorebookEntryResult result) {

        return CreateLorebookEntryResponse.build(result.getId());
    }

    public UpdateLorebookEntryResponse toResponse(UpdateAdventureLorebookEntryResult result) {

        return UpdateLorebookEntryResponse.build(result.getLastUpdatedDateTime());
    }
}
