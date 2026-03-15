package me.moirai.storyengine.infrastructure.outbound.adapter.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntriesResult;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntry;

@Component
public class AdventureLorebookPersistenceMapper {

    public AdventureLorebookEntryDetails mapToResult(AdventureLorebookEntry entry) {

        return AdventureLorebookEntryDetails.builder()
                .id(entry.getPublicId())
                .name(entry.getName())
                .description(entry.getDescription())
                .regex(entry.getRegex())
                .playerId(entry.getPlayerId())
                .isPlayerCharacter(entry.isPlayerCharacter())
                .creationDate(entry.getCreationDate())
                .lastUpdateDate(entry.getLastUpdateDate())
                .build();
    }

    public SearchAdventureLorebookEntriesResult mapToResult(Page<AdventureLorebookEntry> pagedResult) {
        return SearchAdventureLorebookEntriesResult.builder()
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
