package me.moirai.storyengine.core.application.usecase.discord.slashcommands;

import me.moirai.storyengine.common.usecases.UseCase;

public final class HelpCommand extends UseCase<String> {

    private final String commandToDescribe;

    private HelpCommand(String commandToDescribe) {
        this.commandToDescribe = commandToDescribe;
    }

    public static HelpCommand build(String commandToDescribe) {
        return new HelpCommand(commandToDescribe);
    }

    public String getCommandToDescribe() {
        return commandToDescribe;
    }
}
