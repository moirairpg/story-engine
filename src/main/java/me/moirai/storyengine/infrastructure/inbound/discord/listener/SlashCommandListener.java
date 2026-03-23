package me.moirai.storyengine.infrastructure.inbound.discord.listener;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.usecases.UseCaseRunner;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureByChannelId;
import me.moirai.storyengine.core.port.inbound.discord.slashcommands.GoCommand;
import me.moirai.storyengine.core.port.inbound.discord.slashcommands.HelpCommand;
import me.moirai.storyengine.core.port.inbound.discord.slashcommands.RetryCommand;
import me.moirai.storyengine.core.port.inbound.discord.slashcommands.StartCommand;
import me.moirai.storyengine.core.port.inbound.discord.slashcommands.TokenizeInput;
import me.moirai.storyengine.core.port.inbound.discord.slashcommands.TokenizeResult;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

@Component
public class SlashCommandListener extends ListenerAdapter {

    private static final String RETRY_COMMAND = "retry";
    private static final String GO_COMMAND = "go";
    private static final String START_COMMAND = "start";
    private static final String SAY_COMMAND = "say";
    private static final String TOKENIZE_COMMAND = "tokenize";
    private static final String REMEMBER_COMMAND = "remember";
    private static final String NUDGE_COMMAND = "nudge";
    private static final String AUTHORS_NOTE_COMMAND = "authorsnote";
    private static final String BUMP_COMMAND = "bump";
    private static final String HELP_COMMAND = "help";

    private static final String CONTENT = "Content";
    private static final String TOKEN_REPLY_MESSAGE = "**Characters:** %s\n**Tokens:** %s\n**Token IDs:** %s (contains %s total tokens).";
    private static final String TOO_MUCH_CONTENT_TO_TOKENIZE = "Could not tokenize content. Too much content. Please use the web UI to tokenize large text";
    private static final int DISCORD_MAX_LENGTH = 2000;

    private final UseCaseRunner useCaseRunner;
    private final DiscordListenerErrorHandler errorHandler;
    private final DiscordListenerHelper discordListenerHelper;
    private final List<String> goCommandPhrasesBeforeRunning;
    private final List<String> goCommandPhrasesAfterRunning;
    private final List<String> retryCommandPhrasesBeforeRunning;
    private final List<String> retryCommandPhrasesAfterRunning;
    private final List<String> startCommandPhrasesBeforeRunning;
    private final List<String> startCommandPhrasesAfterRunning;

    public SlashCommandListener(
            UseCaseRunner useCaseRunner,
            DiscordListenerErrorHandler errorHandler,
            DiscordListenerHelper discordListenerHelper,
            @Value("${moirai.discord.bot.commands.go.before-running}") List<String> goCommandPhrasesBeforeRunning,
            @Value("${moirai.discord.bot.commands.go.after-running}") List<String> goCommandPhrasesAfterRunning,
            @Value("${moirai.discord.bot.commands.retry.before-running}") List<String> retryCommandPhrasesBeforeRunning,
            @Value("${moirai.discord.bot.commands.retry.after-running}") List<String> retryCommandPhrasesAfterRunning,
            @Value("${moirai.discord.bot.commands.start.before-running}") List<String> startCommandPhrasesBeforeRunning,
            @Value("${moirai.discord.bot.commands.start.after-running}") List<String> startCommandPhrasesAfterRunning) {

        this.useCaseRunner = useCaseRunner;
        this.errorHandler = errorHandler;
        this.discordListenerHelper = discordListenerHelper;
        this.goCommandPhrasesBeforeRunning = goCommandPhrasesBeforeRunning;
        this.goCommandPhrasesAfterRunning = goCommandPhrasesAfterRunning;
        this.retryCommandPhrasesBeforeRunning = retryCommandPhrasesBeforeRunning;
        this.retryCommandPhrasesAfterRunning = retryCommandPhrasesAfterRunning;
        this.startCommandPhrasesBeforeRunning = startCommandPhrasesBeforeRunning;
        this.startCommandPhrasesAfterRunning = startCommandPhrasesAfterRunning;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        try {
            String command = event.getFullCommandName();
            TextChannel textChannel = event.getChannel().asTextChannel();
            Guild guild = event.getGuild();
            Member bot = guild.retrieveMember(event.getJDA().getSelfUser()).complete();
            User author = event.getMember().getUser();

            if (!author.isBot()) {
                switch (command) {
                    case RETRY_COMMAND -> processRetryCommand(event, textChannel, guild, bot);
                    case GO_COMMAND -> processGoCommand(event, textChannel, guild, bot);
                    case START_COMMAND -> processStartCommand(event, textChannel, guild, bot);
                    case SAY_COMMAND -> processSayCommand(event);
                    case TOKENIZE_COMMAND -> processTokenizeCommand(event);
                    case REMEMBER_COMMAND -> processRememberCommand(event, textChannel);
                    case NUDGE_COMMAND -> processNudgeCommand(event, textChannel);
                    case AUTHORS_NOTE_COMMAND -> processAuthorsNoteCommand(event, textChannel);
                    case BUMP_COMMAND -> processBumpCommand(event, textChannel);
                    case HELP_COMMAND -> processHelpCommand(event, textChannel, bot);
                }
            }
        } catch (Exception e) {
            handleError(event, e);
        }
    }

    private void processHelpCommand(SlashCommandInteractionEvent event, TextChannel textChannel, Member bot) {

        String commandToDescribe = EMPTY;
        if (null != event.getOption("command")) {
            commandToDescribe = event.getOption("command").getAsString();
        }

        HelpCommand request = HelpCommand.build(commandToDescribe);
        String helpText = useCaseRunner.run(request);

        event.replyEmbeds(
                new EmbedBuilder()
                        .setDescription(helpText)
                        .setTitle("MoirAI Help")
                        .setThumbnail(bot.getAvatarUrl())
                        .setColor(Color.GREEN)
                        .build())
                .setEphemeral(true)
                .complete();
    }

    private void processTokenizeCommand(SlashCommandInteractionEvent event) {

        InteractionHook interactionHook = discordListenerHelper.sendNotification(event, "Tokenizing input...");
        String inputToBeTokenized = event.getOption("input").getAsString();
        TokenizeResult tokenizationResult = useCaseRunner.run(TokenizeInput.build(inputToBeTokenized))
                .orElseThrow(() -> new IllegalStateException("Error tokenizing input"));

        String finalResult = mapTokenizationResultToMessage(tokenizationResult);

        if (finalResult.length() > DISCORD_MAX_LENGTH) {
            discordListenerHelper.updateNotification(interactionHook, TOO_MUCH_CONTENT_TO_TOKENIZE);
            return;
        }

        discordListenerHelper.updateNotification(interactionHook, finalResult);
    }

    private void processBumpCommand(SlashCommandInteractionEvent event, TextChannel textChannel) {

        GetAdventureByChannelId request = GetAdventureByChannelId.build(textChannel.getId(), event.getMember().getId());
        AdventureDetails result = useCaseRunner.run(request);

        TextInput bumpContent = TextInput.create("bumpContent", CONTENT, TextInputStyle.PARAGRAPH)
                .setPlaceholder("Reminders inserted between messages to keep AI's act on track")
                .setMinLength(1)
                .setMaxLength(50)
                .setValue(result.bump())
                .build();

        TextInput bumpFrequency = TextInput
                .create("bumpFrequency", "Insert bump every # messages", TextInputStyle.SHORT)
                .setPlaceholder("3")
                .setMinLength(1)
                .setMaxLength(2)
                .setValue(String.valueOf(result.bumpFrequency()))
                .build();

        Modal modal = Modal.create(BUMP_COMMAND, "Context modifier: bump")
                .addComponents(ActionRow.of(bumpContent), ActionRow.of(bumpFrequency))
                .build();

        event.replyModal(modal).complete();
    }

    private void processAuthorsNoteCommand(SlashCommandInteractionEvent event, TextChannel textChannel) {

        GetAdventureByChannelId request = GetAdventureByChannelId.build(textChannel.getId(), event.getMember().getId());
        AdventureDetails result = useCaseRunner.run(request);

        TextInput authorsNoteContent = TextInput
                .create("authorsNoteContent", CONTENT, TextInputStyle.PARAGRAPH)
                .setPlaceholder("Instructions from the author on how the story should be told")
                .setMinLength(1)
                .setMaxLength(50)
                .setValue(result.authorsNote())
                .build();

        Modal modal = Modal.create("authorsNote", "Context modifier: author's note")
                .addComponents(ActionRow.of(authorsNoteContent))
                .build();

        event.replyModal(modal).complete();
    }

    private void processNudgeCommand(SlashCommandInteractionEvent event, TextChannel textChannel) {

        GetAdventureByChannelId request = GetAdventureByChannelId.build(textChannel.getId(), event.getMember().getId());
        AdventureDetails result = useCaseRunner.run(request);

        TextInput nudgeContent = TextInput.create("nudgeContent", CONTENT, TextInputStyle.PARAGRAPH)
                .setPlaceholder("General instructions for the AI to follow")
                .setMinLength(1)
                .setMaxLength(50)
                .setValue(result.nudge())
                .build();

        Modal modal = Modal.create(NUDGE_COMMAND, "Context modifier: nudge")
                .addComponents(ActionRow.of(nudgeContent))
                .build();

        event.replyModal(modal).complete();
    }

    private void processRememberCommand(SlashCommandInteractionEvent event, TextChannel textChannel) {

        GetAdventureByChannelId request = GetAdventureByChannelId.build(textChannel.getId(), event.getMember().getId());
        AdventureDetails result = useCaseRunner.run(request);

        TextInput rememberContent = TextInput
                .create("rememberContent", CONTENT, TextInputStyle.PARAGRAPH)
                .setPlaceholder("Important piece of information the AI has to remember about")
                .setMinLength(1)
                .setMaxLength(50)
                .setValue(result.remember())
                .build();

        Modal modal = Modal.create(REMEMBER_COMMAND, "Context modifier: remember")
                .addComponents(ActionRow.of(rememberContent))
                .build();

        event.replyModal(modal).complete();
    }

    private void processSayCommand(SlashCommandInteractionEvent event) {

        event.getChannel().sendTyping().complete();
        TextInput content = TextInput.create("content", CONTENT, TextInputStyle.PARAGRAPH)
                .setPlaceholder("Message to be sent as the bot")
                .setMinLength(1)
                .setMaxLength(2000)
                .build();

        Modal modal = Modal.create("sayAsBot", "Say as bot")
                .addComponents(ActionRow.of(content))
                .build();

        event.replyModal(modal).complete();
    }

    private void processStartCommand(
            SlashCommandInteractionEvent event,
            TextChannel textChannel,
            Guild guild,
            Member bot) {

        event.getChannel().sendTyping().complete();
        InteractionHook interactionHook = discordListenerHelper.sendNotification(event,
                getCommandPhrase(startCommandPhrasesBeforeRunning));

        try {
            String botUsername = bot.getUser().getName();
            String botNickname = isNotBlank(bot.getNickname()) ? bot.getNickname()
                    : botUsername;

            StartCommand useCase = StartCommand.builder()
                    .botId(bot.getId())
                    .botNickname(botNickname)
                    .botUsername(botUsername)
                    .guildId(guild.getId())
                    .channelId(textChannel.getId())
                    .build();

            useCaseRunner.run(useCase);
            discordListenerHelper.updateNotification(interactionHook,
                    getCommandPhrase(startCommandPhrasesAfterRunning));
        } catch (Exception e) {
            discordListenerHelper.updateNotification(interactionHook, e.getMessage());
        }
    }

    private void processGoCommand(SlashCommandInteractionEvent event, TextChannel textChannel,
            Guild guild, Member bot) {

        event.getChannel().sendTyping().complete();
        InteractionHook interactionHook = discordListenerHelper.sendNotification(event,
                getCommandPhrase(goCommandPhrasesBeforeRunning));
        try {
            String botUsername = bot.getUser().getName();
            String botNickname = isNotBlank(bot.getNickname()) ? bot.getNickname()
                    : botUsername;

            GoCommand useCase = GoCommand.builder()
                    .botId(bot.getId())
                    .botNickname(botNickname)
                    .botUsername(botUsername)
                    .guildId(guild.getId())
                    .channelId(textChannel.getId())
                    .build();

            useCaseRunner.run(useCase);
            discordListenerHelper.updateNotification(interactionHook,
                    getCommandPhrase(goCommandPhrasesAfterRunning));
        } catch (Exception e) {
            errorHandler.handleError(interactionHook, e);
        }
    }

    private void processRetryCommand(SlashCommandInteractionEvent event, TextChannel textChannel,
            Guild guild, Member bot) {

        event.getChannel().sendTyping().complete();
        InteractionHook interactionHook = discordListenerHelper.sendNotification(event,
                getCommandPhrase(retryCommandPhrasesBeforeRunning));
        try {
            String botUsername = bot.getUser().getName();
            String botNickname = isNotBlank(bot.getNickname()) ? bot.getNickname()
                    : botUsername;

            RetryCommand useCase = RetryCommand.builder()
                    .botId(bot.getId())
                    .botNickname(botNickname)
                    .botUsername(botUsername)
                    .guildId(guild.getId())
                    .channelId(textChannel.getId())
                    .build();

            useCaseRunner.run(useCase);
            discordListenerHelper.updateNotification(interactionHook,
                    getCommandPhrase(retryCommandPhrasesAfterRunning));
        } catch (Exception e) {
            errorHandler.handleError(interactionHook, e);
        }
    }

    private String mapTokenizationResultToMessage(TokenizeResult tokenizationResult) {

        return String.format(TOKEN_REPLY_MESSAGE, tokenizationResult.characterCount(),
                tokenizationResult.tokens(), Arrays.toString(tokenizationResult.tokenIds()),
                tokenizationResult.tokenCount());
    }

    private String getCommandPhrase(List<String> phraseList) {

        int randomIndex = new Random().nextInt(phraseList.size());
        return phraseList.get(randomIndex);
    }

    private void handleError(SlashCommandInteractionEvent event, Throwable error) {

        Member member = event.getMember();
        MessageChannelUnion channel = event.getChannel();
        errorHandler.handleError(member, channel, error);
    }
}
