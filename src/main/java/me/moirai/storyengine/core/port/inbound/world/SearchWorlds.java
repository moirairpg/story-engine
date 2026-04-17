package me.moirai.storyengine.core.port.inbound.world;

import me.moirai.storyengine.common.cqs.query.Query;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.enums.SearchView;
import me.moirai.storyengine.common.enums.SortDirection;

public record SearchWorlds(
        String name,
        SearchView view,
        WorldSortField sortingField,
        SortDirection direction,
        Integer page,
        Integer size,
        Long requesterId)
        implements Query<PaginatedResult<WorldSummary>> {
}
