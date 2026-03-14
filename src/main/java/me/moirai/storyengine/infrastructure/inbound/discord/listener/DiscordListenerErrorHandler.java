package me.moirai.storyengine.infrastructure.inbound.discord.listener;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.core.port.DiscordChannelPort;
import me.moirai.storyengine.core.port.outbound.DiscordEmbeddedMessageRequest;
import me.moirai.storyengine.core.port.outbound.DiscordEmbeddedMessageRequest.Color;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.InteractionHook;

@Component
public class DiscordListenerErrorHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DiscordListenerErrorHandler.class);

    private static final int ERROR_MESSAGE_TTL = 10;
    private static final String COMMA_DELIMITER = ", ";
    private static final String CONTENT_FLAGGED_MESSAGE = "Message content was flagged by moderation. The following topics were blocked: %s";
    private static final String SOMETHING_WENT_WRONG = "Something went wrong. Please try again.";

    private final DiscordListenerHelper discordListenerHelper;
    private final DiscordChannelPort discordChannelPort;

    public DiscordListenerErrorHandler(
            DiscordListenerHelper discordListenerHelper,
            DiscordChannelPort discordChannelPort) {

        this.discordListenerHelper = discordListenerHelper;
        this.discordChannelPort = discordChannelPort;
    }

    public void handleError(InteractionHook interactionHook, Throwable error) {

        if (error instanceof ModerationException moderationException) {
            String flaggedTopics = String.join(COMMA_DELIMITER, moderationException.getFlaggedTopics());
            String message = String.format(CONTENT_FLAGGED_MESSAGE, flaggedTopics);

            discordListenerHelper.updateNotification(interactionHook, message);
            return;
        }

        else if (error instanceof AssetNotFoundException assetNotFoundException) {
            discordListenerHelper.updateNotification(interactionHook, assetNotFoundException.getMessage());
            return;
        }

        discordListenerHelper.updateNotification(interactionHook, SOMETHING_WENT_WRONG);
    }

    public void handleError(Member member, MessageChannelUnion channel, Throwable error) {

        LOG.error("An error occured while processing message received from Discord", error);
        String authorNickname = isNotBlank(member.getNickname()) ? member.getNickname()
                : member.getUser().getGlobalName();

        DiscordEmbeddedMessageRequest.Builder embedBuilder = DiscordEmbeddedMessageRequest.builder()
                .authorName(authorNickname)
                .authorIconUrl(member.getAvatarUrl())
                .embedColor(Color.RED);

        if (error instanceof ModerationException moderationException) {
            String flaggedTopics = String.join(COMMA_DELIMITER, moderationException.getFlaggedTopics());
            String message = String.format(CONTENT_FLAGGED_MESSAGE, flaggedTopics);

            DiscordEmbeddedMessageRequest embed = embedBuilder.messageContent(message)
                    .titleText("Inappropriate content detected")
                    .footerText("MoirAI content moderation")
                    .build();

            discordChannelPort.sendTemporaryEmbeddedMessageTo(channel.getId(), embed, ERROR_MESSAGE_TTL);
            return;
        }

        else if (error instanceof AssetNotFoundException) {
            DiscordEmbeddedMessageRequest embed = embedBuilder.messageContent(error.getMessage())
                    .titleText("Asset requested was not found")
                    .footerText("MoirAI asset management")
                    .build();

            discordChannelPort.sendTemporaryEmbeddedMessageTo(channel.getId(), embed, ERROR_MESSAGE_TTL);
            return;
        }

        DiscordEmbeddedMessageRequest embed = embedBuilder.messageContent(SOMETHING_WENT_WRONG)
                .titleText("An error occurred")
                .footerText("MoirAI error handling")
                .build();

        discordChannelPort.sendTemporaryEmbeddedMessageTo(channel.getId(), embed, ERROR_MESSAGE_TTL);
    }
}
