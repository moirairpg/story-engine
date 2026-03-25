package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;

public record UpdateAdventureLorebookEntry(
        UUID entryId,
        UUID adventureId,
        String name,
        String regex,
        String description,
        String playerId)
        implements Command<AdventureLorebookEntryDetails> {
}
