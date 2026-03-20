package me.moirai.storyengine.infrastructure.outbound.adapter.mapper;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.domain.world.WorldLorebookEntry;
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
}
