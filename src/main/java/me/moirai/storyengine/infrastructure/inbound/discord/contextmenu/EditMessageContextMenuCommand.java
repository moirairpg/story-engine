package me.moirai.storyengine.infrastructure.inbound.discord.contextmenu;

import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.interactions.commands.Command.Type;

@Component
public class EditMessageContextMenuCommand extends DiscordContextMenuCommand {

    @Override
    public String getName() {
        return "(MoirAI) Edit message";
    }

    @Override
    public Type getCommandType() {
        return Type.MESSAGE;
    }
}
