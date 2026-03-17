package me.moirai.storyengine.infrastructure.outbound.adapter.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntriesResult;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntry;

@Component
public class AdventureLorebookPersistenceMapper {

    public AdventureLorebookEntryDetails mapToResult(AdventureLorebookEntry entry) {

        return new AdventureLorebookEntryDetails(
                entry.getPublicId(),
                null,
                entry.getName(),
                entry.getRegex(),
                entry.getDescription(),
                entry.getPlayerId(),
                entry.isPlayerCharacter(),
                entry.getCreationDate(),
                entry.getLastUpdateDate());
    }

    public SearchAdventureLorebookEntriesResult mapToResult(Page<AdventureLorebookEntry> pagedResult) {
        return new SearchAdventureLorebookEntriesResult(
                pagedResult.getNumber() + 1,
                pagedResult.getNumberOfElements(),
                pagedResult.getTotalElements(),
                pagedResult.getTotalPages(),
                pagedResult.getContent()
                        .stream()
                        .map(this::mapToResult)
                        .toList());
    }
}
