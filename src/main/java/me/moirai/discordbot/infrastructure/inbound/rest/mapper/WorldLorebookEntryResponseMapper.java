package me.moirai.discordbot.infrastructure.inbound.rest.mapper;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import me.moirai.discordbot.core.application.usecase.world.result.CreateWorldLorebookEntryResult;
import me.moirai.discordbot.core.application.usecase.world.result.GetWorldLorebookEntryResult;
import me.moirai.discordbot.core.application.usecase.world.result.SearchWorldLorebookEntriesResult;
import me.moirai.discordbot.core.application.usecase.world.result.UpdateWorldLorebookEntryResult;
import me.moirai.discordbot.infrastructure.inbound.rest.response.CreateLorebookEntryResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.LorebookEntryResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.SearchLorebookEntriesResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.UpdateLorebookEntryResponse;

@Component
public class WorldLorebookEntryResponseMapper {

    public SearchLorebookEntriesResponse toResponse(SearchWorldLorebookEntriesResult result) {

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

    public LorebookEntryResponse toResponse(GetWorldLorebookEntryResult result) {

        return LorebookEntryResponse.builder()
                .id(result.getId())
                .name(result.getName())
                .regex(result.getRegex())
                .description(result.getDescription())
                .creationDate(result.getCreationDate())
                .lastUpdateDate(result.getLastUpdateDate())
                .build();
    }

    public CreateLorebookEntryResponse toResponse(CreateWorldLorebookEntryResult result) {

        return CreateLorebookEntryResponse.build(result.getId());
    }

    public UpdateLorebookEntryResponse toResponse(UpdateWorldLorebookEntryResult result) {

        return UpdateLorebookEntryResponse.build(result.getLastUpdatedDateTime());
    }
}
