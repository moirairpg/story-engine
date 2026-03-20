package me.moirai.storyengine.infrastructure.outbound.adapter.mapper;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
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
}
