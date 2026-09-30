package me.moirai.storyengine.core.port.inbound.userdetails;

import me.moirai.storyengine.common.cqs.command.Command;

public record DeleteUserByUsername(String username) implements Command<Void> {
}
