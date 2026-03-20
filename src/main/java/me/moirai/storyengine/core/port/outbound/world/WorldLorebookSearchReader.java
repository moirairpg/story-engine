package me.moirai.storyengine.core.port.outbound.world;

import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.core.port.inbound.LorebookEntrySummary;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntries;

public interface WorldLorebookSearchReader {

    PaginatedResult<LorebookEntrySummary> search(SearchWorldLorebookEntries query);
}
