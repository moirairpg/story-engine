package me.moirai.storyengine.infrastructure.outbound.adapter.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.domain.world.WorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntriesResult;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;

@Component
public class WorldLorebookPersistenceMapper {

    public WorldLorebookEntryDetails mapToResult(WorldLorebookEntry entry) {

        return new WorldLorebookEntryDetails(
                entry.getPublicId(),
                null,
                entry.getName(),
                entry.getRegex(),
                entry.getDescription(),
                entry.getCreationDate(),
                entry.getLastUpdateDate());
    }

    public SearchWorldLorebookEntriesResult mapToResult(Page<WorldLorebookEntry> pagedResult) {

        return new SearchWorldLorebookEntriesResult(
                pagedResult.getNumber() + 1,
                pagedResult.getNumberOfElements(),
                pagedResult.getTotalElements(),
                pagedResult.getTotalPages(),
                pagedResult.getContent()
                        .stream()
                        .map(entry -> mapToResult(entry))
                        .toList());
    }
}
