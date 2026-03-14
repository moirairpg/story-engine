package me.moirai.storyengine.infrastructure.outbound.adapter.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.application.usecase.world.result.GetWorldResult;
import me.moirai.storyengine.core.application.usecase.world.result.SearchWorldsResult;
import me.moirai.storyengine.core.domain.world.World;

@Component
public class WorldPersistenceMapper {

    public GetWorldResult mapToResult(World world) {

        return GetWorldResult.builder()
                .id(world.getId())
                .name(world.getName())
                .description(world.getDescription())
                .adventureStart(world.getAdventureStart())
                .usersAllowedToRead(world.getUsersAllowedToRead())
                .usersAllowedToWrite(world.getUsersAllowedToWrite())
                .visibility(world.getVisibility().name())
                .ownerId(world.getOwnerId())
                .creationDate(world.getCreationDate())
                .lastUpdateDate(world.getLastUpdateDate())
                .build();
    }

    public SearchWorldsResult mapToResult(Page<World> pagedResult) {

        return SearchWorldsResult.builder()
                .results(pagedResult.getContent()
                        .stream()
                        .map(this::mapToResult)
                        .toList())
                .page(pagedResult.getNumber() + 1)
                .items(pagedResult.getNumberOfElements())
                .totalItems(pagedResult.getTotalElements())
                .totalPages(pagedResult.getTotalPages())
                .build();
    }
}
