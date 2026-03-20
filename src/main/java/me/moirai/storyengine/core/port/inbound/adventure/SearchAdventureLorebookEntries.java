package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.query.Query;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.enums.SortDirection;
import me.moirai.storyengine.core.port.inbound.LorebookEntrySummary;

public record SearchAdventureLorebookEntries(
        UUID adventureId,
        String name,
        AdventureLorebookSortField sortingField,
        SortDirection direction,
        Integer page,
        Integer size,
        String requesterId)
        implements Query<PaginatedResult<LorebookEntrySummary>> {
}
