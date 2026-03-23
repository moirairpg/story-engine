package me.moirai.storyengine.infrastructure.outbound.adapter.discord;

import static me.moirai.storyengine.common.util.DefaultStringProcessors.formatChatMessage;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.awt.Color;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.port.outbound.discord.DiscordChannelPort;
import me.moirai.storyengine.core.port.outbound.discord.DiscordEmbeddedMessageRequest;
import me.moirai.storyengine.core.port.outbound.discord.DiscordMessageData;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDetails;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@Component
public class DiscordChannelAdapter implements DiscordChannelPort {

    private static final String USER_MENTION_PLACEHOLDER = "<@%s>";
    private static final int MAX_DISCORD_MESSAGES_ALLOWED = 100;
    private static final String TEMPORARY_MESSAGE_WARNING = "\n\nThis message will disappear after %s seconds.";

    private final JDA jda;

    @Lazy
    public DiscordChannelAdapter(JDA jda) {

        this.jda = jda;
    }

    /**
     * Sends a text message to the specified channel
     *
     * @param channelId      ID of the channel where the message has to be sent
     * @param messageContent Text content of the message to be sent
     * @return Message's, author's and mentioned users' metadata
     */
    @Override
    public DiscordMessageData sendTextMessageTo(String channelId, String messageContent) {

        Message messageSent = jda.getTextChannelById(channelId)
                .sendMessage(messageContent)
                .complete();

        Member author = messageSent.getGuild()
                .retrieveMemberById(messageSent.getAuthor().getId())
                .complete();

        var nickname = isNotEmpty(author.getNickname()) ? author.getNickname() : author.getUser().getGlobalName();
        return new DiscordMessageData(
                messageSent.getId(),
                channelId,
                messageSent.getContentRaw(),
                DiscordUserDetails.builder()
                        .id(author.getId())
                        .nickname(nickname)
                        .username(author.getUser().getName())
                        .mention(author.getAsMention())
                        .build(),
                List.of());
    }

    /**
     * Sends an embedded message to the specified channel
     *
     * @param channelId      ID of the channel where the message has to be sent
     * @param messageContent Text content of the message to be sent in the embed
     * @return Message's, author's and mentioned users' metadata
     */
    @Override
    public DiscordMessageData sendEmbeddedMessageTo(String channelId, DiscordEmbeddedMessageRequest embedData) {

        EmbedBuilder eb = new EmbedBuilder();
        eb.setDescription(embedData.getMessageContent());
        eb.setFooter(embedData.getFooterText());
        eb.setTitle(embedData.getTitleText());
        eb.setImage(embedData.getImageUrl());
        eb.setThumbnail(embedData.getThumbnailUrl());
        eb.setAuthor(embedData.getAuthorName(), embedData.getAuthorWebsiteUrl(), embedData.getAuthorIconUrl());
        eb.setColor(new Color(embedData.getEmbedColor().getRed(), embedData.getEmbedColor().getGreen(),
                embedData.getEmbedColor().getBlue()));

        Message messageSent = jda.getTextChannelById(channelId)
                .sendMessageEmbeds(eb.build())
                .complete();

        Member author = messageSent.getGuild()
                .retrieveMemberById(messageSent.getAuthor().getId())
                .complete();

        var nickname = isNotEmpty(author.getNickname()) ? author.getNickname() : author.getUser().getGlobalName();
        return new DiscordMessageData(
                messageSent.getId(),
                channelId,
                messageSent.getEmbeds().get(0).getDescription(),
                DiscordUserDetails.builder()
                        .id(author.getId())
                        .nickname(nickname)
                        .username(author.getUser().getName())
                        .mention(author.getAsMention())
                        .build(),
                List.of());
    }

    /**
     * Sends an embedded message to the specified channel that will be deleted after
     * specified time
     *
     * @param channelId          ID of the channel where the message has to be sent
     * @param messageContent     Text content of the message to be sent in the embed
     * @param deleteAfterSeconds TTL in seconds for the message sent
     */
    @Override
    public void sendTemporaryEmbeddedMessageTo(String channelId,
            DiscordEmbeddedMessageRequest embedData, int deleteAfterSeconds) {

        EmbedBuilder eb = new EmbedBuilder();
        eb.setDescription(embedData.getMessageContent());
        eb.setFooter(embedData.getFooterText());
        eb.setTitle(embedData.getTitleText());
        eb.setImage(embedData.getImageUrl());
        eb.setThumbnail(embedData.getThumbnailUrl());
        eb.setAuthor(embedData.getAuthorName(), embedData.getAuthorWebsiteUrl(), embedData.getAuthorIconUrl());
        eb.setColor(new Color(embedData.getEmbedColor().getRed(), embedData.getEmbedColor().getGreen(),
                embedData.getEmbedColor().getBlue()));

        TextChannel channel = jda.getTextChannelById(channelId);
        channel.sendMessageEmbeds(eb.build())
                .complete()
                .delete()
                .completeAfter(deleteAfterSeconds, TimeUnit.SECONDS);
    }

    /**
     * Sends a text message to the specified channel that will be deleted after
     * specified time
     *
     * @param channelId          ID of the channel where the message has to be sent
     * @param messageContent     Text content of the message to be sent
     * @param deleteAfterSeconds TTL in seconds for the message sent
     */
    @Override
    public void sendTemporaryTextMessageTo(String channelId, String messageContent, int deleteAfterSeconds) {

        TextChannel channel = jda.getTextChannelById(channelId);
        channel.sendMessage(messageContent + String.format(TEMPORARY_MESSAGE_WARNING, deleteAfterSeconds))
                .complete()
                .delete()
                .completeAfter(deleteAfterSeconds, TimeUnit.SECONDS);
    }

    @Override
    public Optional<DiscordMessageData> getMessageById(String channelId, String messageId) {
        try {
            Message message = jda.getTextChannelById(channelId)
                    .retrieveMessageById(messageId)
                    .complete();

            if (isBlank(message.getContentRaw())) {
                return Optional.empty();
            }

            Member author = message.getGuild()
                    .retrieveMemberById(message.getAuthor().getId())
                    .complete();

            String authorNickname = isNotEmpty(author.getNickname()) ? author.getNickname()
                    : author.getUser().getGlobalName();

            String formattedContent = formatMessageWithMentions(message.getMentions().getMembers(), message,
                    authorNickname, author.getUser().getName());

            var authorDetails = DiscordUserDetails.builder()
                    .id(author.getId())
                    .nickname(authorNickname)
                    .username(author.getUser().getName())
                    .mention(author.getAsMention())
                    .build();

            var mentionedUsers = message.getMentions()
                    .getMembers().stream()
                    .map(member -> DiscordUserDetails.builder()
                            .id(member.getId())
                            .mention(member.getAsMention())
                            .nickname(isNotEmpty(member.getNickname())
                                    ? member.getNickname()
                                    : member.getUser().getGlobalName())
                            .username(member.getUser().getName())
                            .build())
                    .toList();

            return Optional.of(new DiscordMessageData(
                    message.getId(),
                    channelId,
                    formattedContent,
                    authorDetails,
                    mentionedUsers));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteMessageById(String channelId, String messageId) {

        jda.getTextChannelById(channelId)
                .deleteMessageById(messageId)
                .complete();
    }

    @Override
    public DiscordMessageData editMessageById(String channelId, String messageId, String messageContent) {

        Message msgToEdit = jda.getTextChannelById(channelId)
                .retrieveMessageById(messageId)
                .complete()
                .editMessage(messageContent)
                .complete();

        Member author = msgToEdit.getGuild()
                .retrieveMemberById(msgToEdit.getAuthor().getId())
                .complete();

        String authorNickname = isNotEmpty(author.getNickname()) ? author.getNickname()
                : author.getUser().getGlobalName();

        var authorDetails = DiscordUserDetails.builder()
                .id(author.getId())
                .nickname(authorNickname)
                .username(author.getUser().getName())
                .mention(author.getAsMention())
                .build();

        var mentionedUsers = msgToEdit.getMentions()
                .getMembers().stream()
                .map(member -> DiscordUserDetails.builder()
                        .id(member.getId())
                        .mention(member.getAsMention())
                        .nickname(isNotEmpty(member.getNickname())
                                ? member.getNickname()
                                : member.getUser().getGlobalName())
                        .username(member.getUser().getName())
                        .build())
                .toList();

        return new DiscordMessageData(
                msgToEdit.getId(),
                channelId,
                messageContent,
                authorDetails,
                mentionedUsers);
    }

    /**
     * Returns the last 100 messages in the channel provided
     *
     * @param channelId Text channel ID to be searched for messages
     * @return List with all messages retrieved with their metadata (author details
     *         and mentions formatted)
     */
    @Override
    public List<DiscordMessageData> retrieveEntireHistoryFrom(String channelId) {

        TextChannel channel = jda.getTextChannelById(channelId);

        return MessageHistory.getHistoryFromBeginning(channel)
                .limit(MAX_DISCORD_MESSAGES_ALLOWED)
                .complete()
                .getRetrievedHistory()
                .stream()
                .filter(message -> isNotBlank(message.getContentRaw()))
                .map(message -> buildMessageResult(channelId, message))
                .toList();
    }

    /**
     * Returns the last 100 messages in the channel provided, before the message
     * supplied
     *
     * @param channelId Text channel ID to be searched for messages
     * @return List with all messages retrieved with their metadata (author details
     *         and mentions formatted)
     */
    @Override
    public List<DiscordMessageData> retrieveEntireHistoryBefore(String messageId, String channelId) {

        TextChannel channel = jda.getTextChannelById(channelId);

        return MessageHistory.getHistoryBefore(channel, messageId)
                .limit(MAX_DISCORD_MESSAGES_ALLOWED)
                .complete()
                .getRetrievedHistory()
                .stream()
                .filter(message -> isNotBlank(message.getContentRaw()))
                .map(message -> buildMessageResult(channelId, message))
                .toList();
    }

    @Override
    public Optional<DiscordMessageData> getLastMessageIn(String channelId) {

        TextChannel channel = jda.getTextChannelById(channelId);
        return Optional.of(getMessageById(channelId, channel.getLatestMessageId())
                .orElseGet(() -> retrieveEntireHistoryBefore(channel.getLatestMessageId(), channelId).getFirst()));
    }

    private DiscordMessageData buildMessageResult(String channelId, Message message) {

        Member author = message.getGuild()
                .retrieveMemberById(message.getAuthor().getId())
                .complete();

        List<Member> mentionedUsers = message.getMentions().getMembers();
        String authorNickname = isNotEmpty(author.getNickname()) ? author.getNickname()
                : author.getUser().getGlobalName();

        String formattedContent = formatMessageWithMentions(mentionedUsers, message,
                authorNickname, author.getUser().getName());

        var authorDetails = DiscordUserDetails.builder()
                .id(author.getId())
                .nickname(authorNickname)
                .username(author.getUser().getName())
                .mention(author.getAsMention())
                .build();

        var mentionedUsersDetails = mentionedUsers.stream()
                .map(member -> DiscordUserDetails.builder()
                        .id(member.getId())
                        .mention(member.getAsMention())
                        .nickname(isNotEmpty(member.getNickname()) ? member.getNickname()
                                : member.getUser().getGlobalName())
                        .username(member.getUser().getName())
                        .build())
                .toList();

        return new DiscordMessageData(
                message.getId(),
                channelId,
                formattedContent,
                authorDetails,
                mentionedUsersDetails);
    }

    private String formatMessageWithMentions(List<Member> mentionedUsers,
            Message message, String authorNickname, String authorUsername) {

        String messageContent = message.getContentRaw();
        for (Member user : mentionedUsers) {
            messageContent = formatContent(user, messageContent);
        }

        return formatChatMessage(authorNickname, authorUsername).apply(messageContent);
    }

    private String formatContent(Member user, String formattedContent) {

        return formattedContent.replace(
                String.format(USER_MENTION_PLACEHOLDER, user.getId()), user.getNickname());
    }
}
