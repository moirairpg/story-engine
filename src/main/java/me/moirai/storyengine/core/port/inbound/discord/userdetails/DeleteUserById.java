package me.moirai.storyengine.core.port.inbound.discord.userdetails;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;

public record DeleteUserById(UUID userId) implements Command<Void> {
}
