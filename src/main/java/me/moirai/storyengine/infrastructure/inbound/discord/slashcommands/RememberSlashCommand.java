package me.moirai.storyengine.infrastructure.inbound.discord.slashcommands;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class RememberSlashCommand extends DiscordSlashCommand {

    @Override
    public String getName() {
        return "remember";
    }

    @Override
    public String getDescription() {
        return "Adds a note of an important piece of information the AI should be aware about";
    }

    @Override
    public List<DiscordSlashCommandOption> getOptions() {
        return Collections.emptyList();
    }
}
