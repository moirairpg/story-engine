package me.moirai.storyengine.core.port.outbound.world;

import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.core.port.inbound.world.SearchWorlds;

public interface WorldSearchReader {

    PaginatedResult<WorldSearchRow> search(SearchWorlds query);
}
