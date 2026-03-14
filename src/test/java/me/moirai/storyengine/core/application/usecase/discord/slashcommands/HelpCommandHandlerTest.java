package me.moirai.storyengine.core.application.usecase.discord.slashcommands;

import static java.lang.String.format;
import static java.lang.String.join;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.LF;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;
import static org.mockito.Mockito.spy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.infrastructure.inbound.discord.slashcommands.DiscordSlashCommand;
import me.moirai.storyengine.infrastructure.inbound.discord.slashcommands.GoSlashCommand;
import me.moirai.storyengine.infrastructure.inbound.discord.slashcommands.HelpSlashCommand;
import me.moirai.storyengine.infrastructure.inbound.discord.slashcommands.RetrySlashCommand;
import me.moirai.storyengine.infrastructure.inbound.discord.slashcommands.TokenizeSlashCommand;

@ExtendWith(MockitoExtension.class)
public class HelpCommandHandlerTest {

    private static final String COMMAND_DESCRIPTION = "* **/%s:** %s";
    private static final String COMMAND_OPTION_DESCRIPTION = "  * **%s:** %s";

    private HelpCommandHandler handler;
    private List<DiscordSlashCommand> commands;

    @BeforeEach
    public void beforeAll() {

        commands = list(spy(HelpSlashCommand.class),
                spy(TokenizeSlashCommand.class),
                spy(RetrySlashCommand.class),
                spy(GoSlashCommand.class));

        handler = new HelpCommandHandler(commands);
    }

    @Test
    public void helpCommand_whenNoSpecificCommandRequested_thenProvideHelpForAll() {

        // Given
        HelpCommand request = HelpCommand.build(EMPTY);
        String expectedResult = join(LF, commands.stream()
                .map(this::processCommand)
                .toList());

        // When
        String result = handler.execute(request);

        // Then
        assertThat(result).isNotNull()
                .isNotEmpty()
                .isEqualTo(expectedResult);
    }

    @Test
    public void helpCommand_whenSpecificCommandRequested_thenDescribeCommand() {

        // Given
        String requestedCommand = "tokenize";
        HelpCommand request = HelpCommand.build(requestedCommand);
        String expectedResult = commands.stream()
                .filter(cmd -> request.getCommandToDescribe().equals(cmd.getName()))
                .findFirst()
                .map(this::processCommand)
                .orElse(EMPTY);

        // When
        String result = handler.execute(request);

        // Then
        assertThat(result).isNotNull()
                .isNotEmpty()
                .isEqualTo(expectedResult);
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
