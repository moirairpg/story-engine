package me.moirai.storyengine.infrastructure.inbound.discord.slashcommands;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class GoSlashCommand extends DiscordSlashCommand {

    @Override
    public String getName() {
        return "go";
    }

    @Override
    public String getDescription() {
        return "Generates output";
    }

    @Override
    public List<DiscordSlashCommandOption> getOptions() {
        return Collections.emptyList();
    }
}
