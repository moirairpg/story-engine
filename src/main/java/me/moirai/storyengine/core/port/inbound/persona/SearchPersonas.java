package me.moirai.storyengine.core.port.inbound.persona;

import me.moirai.storyengine.common.cqs.query.Query;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.enums.SearchView;
import me.moirai.storyengine.common.enums.SortDirection;

public record SearchPersonas(
        String name,
        SearchView view,
        PersonaSortField sortingField,
        SortDirection direction,
        Integer page,
        Integer size,
        Long requesterId) implements Query<PaginatedResult<PersonaSummary>> {
}
