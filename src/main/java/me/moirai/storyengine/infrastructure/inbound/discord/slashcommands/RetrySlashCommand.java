package me.moirai.storyengine.infrastructure.inbound.discord.slashcommands;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class RetrySlashCommand extends DiscordSlashCommand {

    @Override
    public String getName() {
        return "retry";
    }

    @Override
    public String getDescription() {
        return "Deletes the bot's last message and generates a new one with new content";
    }

    @Override
    public List<DiscordSlashCommandOption> getOptions() {
        return Collections.emptyList();
    }
}
