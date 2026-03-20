package me.moirai.storyengine.core.port.outbound.world;

import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.core.port.inbound.world.SearchWorlds;
import me.moirai.storyengine.core.port.inbound.world.WorldSummary;

public interface WorldSearchReader {

    PaginatedResult<WorldSummary> search(SearchWorlds query);
}
