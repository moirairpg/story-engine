package me.moirai.storyengine.core.port.inbound.world;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.query.Query;

public record SearchWorldLorebookEntries(
        String name,
        UUID worldId,
        Integer page,
        Integer size,
        String sortingField,
        String direction,
        String requesterId)
        implements Query<SearchWorldLorebookEntriesResult> {
}
