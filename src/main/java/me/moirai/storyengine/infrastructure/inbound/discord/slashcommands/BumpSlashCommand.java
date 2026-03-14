package me.moirai.storyengine.infrastructure.inbound.discord.slashcommands;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class BumpSlashCommand extends DiscordSlashCommand {

    @Override
    public String getName() {
        return "bump";
    }

    @Override
    public String getDescription() {
        return "Adds behavioral reminders between messages to keep the AI's act on track";
    }

    @Override
    public List<DiscordSlashCommandOption> getOptions() {
        return Collections.emptyList();
    }
}
