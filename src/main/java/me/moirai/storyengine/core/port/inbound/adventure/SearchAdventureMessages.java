package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.query.Query;
import me.moirai.storyengine.common.dto.PaginatedResult;

public record SearchAdventureMessages(
        UUID adventureId,
        Integer page,
        Integer size)
        implements Query<PaginatedResult<MessageSummary>> {
}
