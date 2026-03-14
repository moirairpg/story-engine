package me.moirai.storyengine.infrastructure.inbound.discord.listener;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Arrays;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.usecases.UseCaseRunner;
import me.moirai.storyengine.core.application.usecase.discord.slashcommands.TokenizeInput;
import me.moirai.storyengine.core.application.usecase.discord.slashcommands.TokenizeResult;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

@Component
public class ContextMenuCommandListener extends ListenerAdapter {

    private static final String TOKEN_REPLY_MESSAGE = "**Characters:** %s\n**Tokens:** %s\n**Token IDs:** %s (contains %s total tokens).";
    private static final String TOO_MUCH_CONTENT_TO_TOKENIZE = "Could not tokenize content. Too much content. Please use the web UI to tokenize large text";
    private static final int DISCORD_MAX_LENGTH = 2000;

    private final UseCaseRunner useCaseRunner;
    private final DiscordListenerHelper discordListenerHelper;
    private final DiscordListenerErrorHandler errorHandler;

    public ContextMenuCommandListener(
            UseCaseRunner useCaseRunner,
            DiscordListenerHelper discordListenerHelper,
            DiscordListenerErrorHandler errorHandler) {

        this.useCaseRunner = useCaseRunner;
        this.discordListenerHelper = discordListenerHelper;
        this.errorHandler = errorHandler;
    }

    @Override
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
        try {
            String commandName = event.getName();
            Member author = event.getMember();
            Member bot = event.getGuild().retrieveMember(event.getJDA().getSelfUser()).complete();
            Message message = event.getTarget();

            if (!author.getUser().isBot()) {
                event.getChannel().sendTyping().complete();

                switch (commandName) {
                    case "(MoirAI) Edit message" -> {
                        String botNickname = isNotBlank(bot.getNickname()) ? bot.getNickname()
                                : bot.getUser().getName();

                        if (!message.getAuthor().getId().equals(bot.getId())) {
                            discordListenerHelper.sendNotification(event, "It's only possible to edit messages sent by " + botNickname);
                            return;
                        }

                        TextInput messageId = TextInput.create("messageId", "Message ID", TextInputStyle.SHORT)
                                .setPlaceholder("Message ID")
                                .setMinLength(1)
                                .setMaxLength(50)
                                .setValue(message.getId())
                                .build();

                        TextInput content = TextInput.create("content", "Content", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("Message content goes here")
                                .setMinLength(1)
                                .setMaxLength(2000)
                                .setValue(message.getContentRaw())
                                .build();

                        Modal modal = Modal.create("editMessage", "Edit message")
                                .addComponents(ActionRow.of(messageId), ActionRow.of(content))
                                .build();

                        event.replyModal(modal).complete();
                    }
                    case "(MoirAI) Tokenize content" -> {
                        InteractionHook interactionHook = discordListenerHelper.sendNotification(event, "Tokenizing input...");
                        String inputToBeTokenized = message.getContentRaw();

                        TokenizeResult tokenizationResult = useCaseRunner.run(TokenizeInput.build(inputToBeTokenized))
                                .orElseThrow(() -> new IllegalStateException("Error tokenizing input"));

                        String finalResult = mapTokenizationResultToMessage(tokenizationResult);

                        if (finalResult.length() > DISCORD_MAX_LENGTH) {
                            discordListenerHelper.updateNotification(interactionHook, TOO_MUCH_CONTENT_TO_TOKENIZE);
                            return;
                        }

                        discordListenerHelper.updateNotification(interactionHook, finalResult);
                    }
                }
            }
        } catch (Exception e) {
            handleError(event, e);
        }
    }

    private String mapTokenizationResultToMessage(TokenizeResult tokenizationResult) {

        return String.format(TOKEN_REPLY_MESSAGE, tokenizationResult.getCharacterCount(),
                tokenizationResult.getTokens(), Arrays.toString(tokenizationResult.getTokenIds()),
                tokenizationResult.getTokenCount());
    }

    private void handleError(MessageContextInteractionEvent event, Throwable error) {

        Member member = event.getMember();
        MessageChannelUnion channel = event.getChannel();
        errorHandler.handleError(member, channel, error);
    }
}
