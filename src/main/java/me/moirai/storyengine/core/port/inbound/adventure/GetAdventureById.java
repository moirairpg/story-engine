package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.query.Query;

public record GetAdventureById(
        UUID adventureId,
        String requesterId)
        implements Query<AdventureDetails> {
}
