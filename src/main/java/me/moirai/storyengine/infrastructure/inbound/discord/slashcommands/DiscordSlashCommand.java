package me.moirai.storyengine.infrastructure.inbound.discord.slashcommands;

import java.util.List;

public abstract class DiscordSlashCommand {

    public abstract String getName();

    public abstract String getDescription();

    public abstract List<DiscordSlashCommandOption> getOptions();
}
