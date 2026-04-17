package me.moirai.storyengine.core.port.inbound.world;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;

public record DeleteWorldLorebookEntry(
        UUID entryId,
        UUID worldId)
        implements Command<Void> {
}
