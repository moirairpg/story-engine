package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.query.Query;

public record AdventureCatchUp(UUID adventureId)
        implements Query<CatchUpResult> {
}
