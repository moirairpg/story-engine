package me.moirai.storyengine.core.application.usecase.discord.slashcommands;

import static com.nimbusds.oauth2.sdk.util.CollectionUtils.isNotEmpty;
import static io.micrometer.common.util.StringUtils.isBlank;
import static java.lang.String.format;
import static java.lang.String.join;
import static org.apache.commons.lang3.StringUtils.LF;

import java.util.List;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.infrastructure.inbound.discord.slashcommands.DiscordSlashCommand;

@UseCaseHandler
public class HelpCommandHandler extends AbstractUseCaseHandler<HelpCommand, String> {

    private static final String COMMAND_REQUESTED_NOT_FOUND = "Command requested not found";
    private static final String COMMAND_DESCRIPTION = "* **/%s:** %s";
    private static final String COMMAND_OPTION_DESCRIPTION = "  * **%s:** %s";

    private final List<DiscordSlashCommand> commands;

    public HelpCommandHandler(List<DiscordSlashCommand> commands) {

        this.commands = commands;
    }

    @Override
    public String execute(HelpCommand useCase) {

        if (isBlank(useCase.getCommandToDescribe())) {

            return join(LF, commands.stream()
                    .map(this::processCommand)
                    .toList());
        }

        return commands.stream()
                .filter(cmd -> useCase.getCommandToDescribe().equals(cmd.getName()))
                .findFirst()
                .map(this::processCommand)
                .orElse(COMMAND_REQUESTED_NOT_FOUND);
    }

    private String processCommand(DiscordSlashCommand cmd) {

        String formattedResult = format(COMMAND_DESCRIPTION, cmd.getName(), cmd.getDescription());
        if (isNotEmpty(cmd.getOptions())) {
            String optionDescriptions = join(LF, cmd.getOptions().stream()
                    .map(option -> format(COMMAND_OPTION_DESCRIPTION, option.getName(),
                            option.getDescription()))
                    .toList());

            return formattedResult + LF + optionDescriptions;
        }

        return formattedResult;
    }
}