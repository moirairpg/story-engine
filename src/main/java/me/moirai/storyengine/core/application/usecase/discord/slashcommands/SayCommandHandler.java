package me.moirai.storyengine.core.application.usecase.discord.slashcommands;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.application.port.DiscordChannelPort;

@UseCaseHandler
public class SayCommandHandler extends AbstractUseCaseHandler<SayCommand, Void> {

    private final DiscordChannelPort discordChannelPort;

    public SayCommandHandler(DiscordChannelPort discordChannelPort) {

        this.discordChannelPort = discordChannelPort;
    }

    @Override
    public Void execute(SayCommand useCase) {

        discordChannelPort.sendTextMessageTo(useCase.getChannelId(), useCase.getMessageContent());

        return null;
    }
}