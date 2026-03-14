package me.moirai.storyengine.infrastructure.inbound.discord.slashcommands;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public final class DiscordSlashCommandOption extends OptionData {

    private DiscordSlashCommandOption(String name, String description, OptionType type) {

        super(type, name, description);
    }

    public static DiscordSlashCommandOption build(String name, String description, OptionType type) {
        return new DiscordSlashCommandOption(name, description, type);
    }
}