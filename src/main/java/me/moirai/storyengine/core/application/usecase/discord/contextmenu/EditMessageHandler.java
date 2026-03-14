package me.moirai.storyengine.core.application.usecase.discord.contextmenu;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.discord.contextmenu.EditMessage;
import me.moirai.storyengine.core.port.outbound.discord.DiscordChannelPort;

@UseCaseHandler
public class EditMessageHandler extends AbstractUseCaseHandler<EditMessage, Void> {

    private final DiscordChannelPort discordChannelPort;

    public EditMessageHandler(DiscordChannelPort discordChannelPort) {
        this.discordChannelPort = discordChannelPort;
    }

    @Override
    public Void execute(EditMessage useCase) {

        discordChannelPort.editMessageById(useCase.getChannelId(), useCase.getMessageId(), useCase.getMessageContent());

        return null;
    }
}
