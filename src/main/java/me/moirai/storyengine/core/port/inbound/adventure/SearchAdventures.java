package me.moirai.storyengine.core.port.inbound.adventure;

import me.moirai.storyengine.common.cqs.query.Query;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.enums.SearchView;
import me.moirai.storyengine.common.enums.SortDirection;

public record SearchAdventures(
        String name,
        String worldName,
        String personaName,
        Boolean isMultiplayer,
        String model,
        String gameMode,
        String moderation,
        SearchView view,
        AdventureSortField sortingField,
        SortDirection direction,
        Integer page,
        Integer size,
        Long requesterId)
        implements Query<PaginatedResult<AdventureSummary>> {
}
