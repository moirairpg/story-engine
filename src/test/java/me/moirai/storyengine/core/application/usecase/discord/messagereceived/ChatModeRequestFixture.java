package me.moirai.storyengine.core.application.usecase.discord.messagereceived;

import me.moirai.storyengine.core.port.inbound.discord.messagereceived.ChatModeRequest;

import java.util.Collections;

public class ChatModeRequestFixture {

    public static ChatModeRequest.Builder create() {

        return ChatModeRequest.builder()
                .authordDiscordId("John")
                .botUsername("TestBot")
                .botNickname("BotNickname")
                .isBotMentioned(false)
                .mentionedUsersIds(Collections.emptyList())
                .channelId("CHNLID")
                .guildId("GLDID")
                .messageId("MSGID");
    }
}
