package me.moirai.storyengine.core.port.inbound.userdetails;

import me.moirai.storyengine.common.cqs.command.Command;

public record UpdateUserDetails(
        String username,
        String displayName,
        String bio) implements Command<Void> {
}
