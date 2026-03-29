package me.moirai.storyengine.core.port.inbound.world;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;

public record CreateWorldLorebookEntry(
        UUID worldId,
        String name,
        String description)
        implements Command<WorldLorebookEntryDetails> {
}