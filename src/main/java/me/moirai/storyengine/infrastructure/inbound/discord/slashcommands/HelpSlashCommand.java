package me.moirai.storyengine.infrastructure.inbound.discord.slashcommands;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.interactions.commands.OptionType;

@Component
public class HelpSlashCommand extends DiscordSlashCommand {

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Shows details about available commands";
    }

    @Override
    public List<DiscordSlashCommandOption> getOptions() {

        List<DiscordSlashCommandOption> options = new ArrayList<>();

        options.add(DiscordSlashCommandOption
                .build("command", "Specific command to be described", OptionType.STRING));

        return options;
    }
}
