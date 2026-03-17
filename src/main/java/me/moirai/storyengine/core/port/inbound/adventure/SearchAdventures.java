package me.moirai.storyengine.core.port.inbound.adventure;

import me.moirai.storyengine.common.cqs.query.Query;

public record SearchAdventures(
        String name,
        String worldName,
        String personaName,
        String ownerId,
        boolean isMultiplayer,
        Integer page,
        Integer size,
        String model,
        String gameMode,
        String moderation,
        String sortingField,
        String direction,
        String visibility,
        String operation,
        String requesterId)
        implements Query<SearchAdventuresResult> {
}
