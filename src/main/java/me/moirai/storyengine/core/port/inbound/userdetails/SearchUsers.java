package me.moirai.storyengine.core.port.inbound.userdetails;

import java.time.Instant;

import me.moirai.storyengine.common.cqs.query.Query;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.enums.SortDirection;

public record SearchUsers(
        String username,
        Role role,
        Boolean isActive,
        Instant registeredFrom,
        Instant registeredTo,
        UserSortField sortingField,
        SortDirection direction,
        Integer page,
        Integer size)
        implements Query<PaginatedResult<UserSummary>> {
}
