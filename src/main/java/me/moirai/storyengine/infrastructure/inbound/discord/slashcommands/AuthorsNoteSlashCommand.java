package me.moirai.storyengine.infrastructure.inbound.discord.slashcommands;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class AuthorsNoteSlashCommand extends DiscordSlashCommand {

    @Override
    public String getName() {
        return "authorsnote";
    }

    @Override
    public String getDescription() {
        return "Adds instructions from the author to the AI as to how the story should be told";
    }

    @Override
    public List<DiscordSlashCommandOption> getOptions() {
        return Collections.emptyList();
    }
}
