package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.query.Query;

public record GetAdventureLorebookEntryById(
        UUID entryId,
        UUID adventureId,
        String requesterId)
        implements Query<AdventureLorebookEntryDetails> {
}
