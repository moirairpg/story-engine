package me.moirai.storyengine.core.domain.world;

import java.util.List;
import java.util.Optional;

import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntries;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntriesResult;

public interface WorldLorebookEntryRepository {

    WorldLorebookEntry save(WorldLorebookEntry lorebookEntry);

    Optional<WorldLorebookEntry> findById(String lorebookEntryId);

    SearchWorldLorebookEntriesResult search(SearchWorldLorebookEntries query);

    void deleteById(String id);

    List<WorldLorebookEntry> findAllByRegex(String regex, String worldId);

    List<WorldLorebookEntry> findAllByWorldId(String worldId);
}
