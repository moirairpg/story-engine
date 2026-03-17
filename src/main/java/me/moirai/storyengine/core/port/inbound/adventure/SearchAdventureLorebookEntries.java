package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.query.Query;

public record SearchAdventureLorebookEntries(
        UUID adventureId,
        String name,
        Integer page,
        Integer size,
        String sortingField,
        String direction,
        String requesterId)
        implements Query<SearchAdventureLorebookEntriesResult> {
}
