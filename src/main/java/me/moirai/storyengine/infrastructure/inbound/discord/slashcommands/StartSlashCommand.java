package me.moirai.storyengine.infrastructure.inbound.discord.slashcommands;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class StartSlashCommand extends DiscordSlashCommand {

    @Override
    public String getName() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "Starts the adventure";
    }

    @Override
    public List<DiscordSlashCommandOption> getOptions() {
        return Collections.emptyList();
    }
}
