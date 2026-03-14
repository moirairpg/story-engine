package me.moirai.storyengine.core.domain.adventure;

import java.util.List;

import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventureLorebookEntry;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventureLorebookEntry;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureLorebookEntryById;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureLorebookEntry;
import reactor.core.publisher.Mono;

public interface AdventureService {

    Mono<AdventureLorebookEntry> createLorebookEntry(CreateAdventureLorebookEntry command);

    Mono<AdventureLorebookEntry> updateLorebookEntry(UpdateAdventureLorebookEntry command);

    List<AdventureLorebookEntry> findAllLorebookEntriesByRegex(String adventureId, String valueToSearch);

    AdventureLorebookEntry findLorebookEntryByPlayerDiscordId(String adventureId, String playerId);

    AdventureLorebookEntry findLorebookEntryById(GetAdventureLorebookEntryById query);

    void deleteLorebookEntry(DeleteAdventureLorebookEntry command);
}
