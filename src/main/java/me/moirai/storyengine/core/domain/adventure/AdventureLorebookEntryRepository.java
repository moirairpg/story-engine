package me.moirai.storyengine.core.domain.adventure;

import java.util.List;
import java.util.Optional;

import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntries;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntriesResult;

public interface AdventureLorebookEntryRepository {

    AdventureLorebookEntry save(AdventureLorebookEntry lorebookEntry);

    Optional<AdventureLorebookEntry> findById(String lorebookEntryId);

    SearchAdventureLorebookEntriesResult search(SearchAdventureLorebookEntries query);

    void deleteById(String id);

    List<AdventureLorebookEntry> findAllByRegex(String regex, String adventureId);

    Optional<AdventureLorebookEntry> findByPlayerId(String playerId, String adventureId);
}
