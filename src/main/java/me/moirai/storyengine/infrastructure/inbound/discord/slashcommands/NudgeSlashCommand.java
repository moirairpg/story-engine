package me.moirai.storyengine.infrastructure.inbound.discord.slashcommands;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class NudgeSlashCommand extends DiscordSlashCommand {

    @Override
    public String getName() {
        return "nudge";
    }

    @Override
    public String getDescription() {
        return "Adds a general instructions as to how the AI should behave";
    }

    @Override
    public List<DiscordSlashCommandOption> getOptions() {
        return Collections.emptyList();
    }
}
