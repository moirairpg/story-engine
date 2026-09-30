package me.moirai.storyengine.core.port.inbound.userdetails;

import me.moirai.storyengine.common.cqs.command.Command;

public record CreateUser(
        String discordToken,
        String username,
        String displayName) implements Command<Void> {
}
