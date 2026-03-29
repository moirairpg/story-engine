package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;

public record CreateAdventureLorebookEntry(
        UUID adventureId,
        String name,
        String description,
        String playerId)
        implements Command<AdventureLorebookEntryDetails> {
}
