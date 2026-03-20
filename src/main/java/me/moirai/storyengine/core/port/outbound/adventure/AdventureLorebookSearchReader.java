package me.moirai.storyengine.core.port.outbound.adventure;

import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.core.port.inbound.LorebookEntrySummary;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntries;

public interface AdventureLorebookSearchReader {

    PaginatedResult<LorebookEntrySummary> search(SearchAdventureLorebookEntries query);
}
