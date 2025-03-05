package me.moirai.discordbot.infrastructure.inbound.rest.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import me.moirai.discordbot.core.application.usecase.world.result.CreateWorldResult;
import me.moirai.discordbot.core.application.usecase.world.result.GetWorldResult;
import me.moirai.discordbot.core.application.usecase.world.result.SearchWorldsResult;
import me.moirai.discordbot.core.application.usecase.world.result.UpdateWorldResult;
import me.moirai.discordbot.infrastructure.inbound.rest.response.CreateWorldResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.SearchWorldsResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.UpdateWorldResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.WorldResponse;

@Component
public class WorldResponseMapper {

    public SearchWorldsResponse toResponse(SearchWorldsResult result) {

        List<WorldResponse> worlds = result.getResults()
                .stream()
                .map(this::toResponse)
                .toList();

        return SearchWorldsResponse.builder()
                .page(result.getPage())
                .resultsInPage(result.getItems())
                .totalPages(result.getTotalPages())
                .totalResults(result.getTotalItems())
                .results(worlds)
                .build();
    }

    public WorldResponse toResponse(GetWorldResult result) {

        return WorldResponse.builder()
                .id(result.getId())
                .name(result.getName())
                .description(result.getDescription())
                .adventureStart(result.getAdventureStart())
                .visibility(result.getVisibility())
                .ownerDiscordId(result.getOwnerDiscordId())
                .creationDate(result.getCreationDate())
                .lastUpdateDate(result.getLastUpdateDate())
                .build();
    }

    public CreateWorldResponse toResponse(CreateWorldResult result) {

        return CreateWorldResponse.build(result.getId());
    }

    public UpdateWorldResponse toResponse(UpdateWorldResult result) {

        return UpdateWorldResponse.build(result.getLastUpdatedDateTime());
    }
}
