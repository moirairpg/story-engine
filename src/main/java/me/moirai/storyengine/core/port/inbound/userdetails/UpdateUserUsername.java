package me.moirai.storyengine.core.port.inbound.userdetails;

import me.moirai.storyengine.common.cqs.command.Command;

public record UpdateUserUsername(String username, String newUsername) implements Command<Void> {
}
