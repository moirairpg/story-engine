package me.moirai.storyengine.core.port.inbound.world;

import java.util.List;
import java.util.Set;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.common.enums.Visibility;

public record CreateWorld(
        String name,
        String description,
        String adventureStart,
        Visibility visibility,
        List<LorebookEntry> lorebookEntries,
        Set<Long> usersAllowedToWrite,
        Set<Long> usersAllowedToRead,
        String requesterId)
        implements Command<WorldDetails> {

    public record LorebookEntry(
            String name,
            String regex,
            String description) {
    }
}
