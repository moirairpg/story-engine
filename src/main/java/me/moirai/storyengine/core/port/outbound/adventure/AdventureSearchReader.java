package me.moirai.storyengine.core.port.outbound.adventure;

import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureSummary;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventures;

public interface AdventureSearchReader {

    PaginatedResult<AdventureSummary> search(SearchAdventures query);
}
