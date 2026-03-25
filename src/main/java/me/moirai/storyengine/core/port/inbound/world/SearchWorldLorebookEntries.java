package me.moirai.storyengine.core.port.inbound.world;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.query.Query;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.enums.SortDirection;
import me.moirai.storyengine.core.port.inbound.LorebookEntrySummary;

public record SearchWorldLorebookEntries(
        UUID worldId,
        String name,
        WorldLorebookSortField sortingField,
        SortDirection direction,
        Integer page,
        Integer size)
        implements Query<PaginatedResult<LorebookEntrySummary>> {
}
