package me.moirai.storyengine.core.port.inbound.world;

import me.moirai.storyengine.common.cqs.query.Query;

public record SearchWorlds(
        String name,
        String ownerId,
        Integer page,
        Integer size,
        String sortingField,
        String direction,
        String visibility,
        String operation,
        String requesterId)
        implements Query<SearchWorldsResult> {
}