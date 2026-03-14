package me.moirai.storyengine.infrastructure.inbound.discord.contextmenu;

import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.interactions.commands.Command.Type;

@Component
public class TokenizeContextMenuCommand extends DiscordContextMenuCommand {

    @Override
    public String getName() {
        return "(MoirAI) Tokenize content";
    }

    @Override
    public Type getCommandType() {
        return Type.MESSAGE;
    }
}
