package me.moirai.storyengine.infrastructure.outbound.adapter.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntriesResult;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntry;

@Component
public class WorldLorebookPersistenceMapper {

    public WorldLorebookEntryDetails mapToResult(WorldLorebookEntry entry) {

        return WorldLorebookEntryDetails.builder()
                .id(entry.getPublicId())
                .name(entry.getName())
                .description(entry.getDescription())
                .regex(entry.getRegex())
                .creationDate(entry.getCreationDate())
                .lastUpdateDate(entry.getLastUpdateDate())
                .build();
    }

    public SearchWorldLorebookEntriesResult mapToResult(Page<WorldLorebookEntry> pagedResult) {
        return SearchWorldLorebookEntriesResult.builder()
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
