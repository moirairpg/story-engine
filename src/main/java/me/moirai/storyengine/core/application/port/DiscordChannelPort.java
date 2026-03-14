package me.moirai.storyengine.core.application.port;

import java.util.List;
import java.util.Optional;

import me.moirai.storyengine.core.application.usecase.discord.DiscordMessageData;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.DiscordEmbeddedMessageRequest;

public interface DiscordChannelPort {

    DiscordMessageData sendTextMessageTo(String channelId, String messageContent);

    DiscordMessageData sendEmbeddedMessageTo(String channelId, DiscordEmbeddedMessageRequest embedData);

    void sendTemporaryEmbeddedMessageTo(String channelId,
            DiscordEmbeddedMessageRequest embedData, int deleteAfterSeconds);

    void sendTemporaryTextMessageTo(String channelId, String messageContent, int deleteAfterSeconds);

    Optional<DiscordMessageData> getMessageById(String channelId, String messageId);

    void deleteMessageById(String channelId, String messageId);

    DiscordMessageData editMessageById(String channelId, String messageId, String messageContent);

    List<DiscordMessageData> retrieveEntireHistoryFrom(String channelId);

    List<DiscordMessageData> retrieveEntireHistoryBefore(String messageId, String channelId);

    Optional<DiscordMessageData> getLastMessageIn(String channelId);
}
