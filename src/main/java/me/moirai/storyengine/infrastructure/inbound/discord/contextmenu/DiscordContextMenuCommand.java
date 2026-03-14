package me.moirai.storyengine.infrastructure.inbound.discord.contextmenu;

import net.dv8tion.jda.api.interactions.commands.Command;

public abstract class DiscordContextMenuCommand {

    public abstract String getName();

    public abstract Command.Type getCommandType();
}
