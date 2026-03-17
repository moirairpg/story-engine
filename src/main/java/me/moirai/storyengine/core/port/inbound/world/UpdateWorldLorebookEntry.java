package me.moirai.storyengine.core.port.inbound.world;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;

public record UpdateWorldLorebookEntry(
        UUID entryId,
        UUID worldId,
        String name,
        String regex,
        String description,
        String requesterId)
        implements Command<WorldLorebookEntryDetails> {
}
