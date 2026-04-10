package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.query.Query;
import me.moirai.storyengine.common.dto.CursorResult;

public record SearchAdventureMessages(
        UUID adventureId,
        UUID lastMessageId,
        int size)
        implements Query<CursorResult<MessageSummary>> {
}
