package me.moirai.storyengine.infrastructure.inbound.discord.slashcommands;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class SaySlashCommand extends DiscordSlashCommand {

    @Override
    public String getName() {
        return "say";
    }

    @Override
    public String getDescription() {
        return "Speaks on behalf of the bot";
    }

    @Override
    public List<DiscordSlashCommandOption> getOptions() {
        return Collections.emptyList();
    }
}
