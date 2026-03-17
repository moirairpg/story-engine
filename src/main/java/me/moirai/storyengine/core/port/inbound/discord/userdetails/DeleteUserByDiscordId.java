package me.moirai.storyengine.core.port.inbound.discord.userdetails;

import me.moirai.storyengine.common.cqs.command.Command;

public record DeleteUserByDiscordId(String discordUserId) implements Command<Void> {
}
