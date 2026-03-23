package me.moirai.storyengine.core.port.outbound.discord;

import java.util.ArrayList;
import java.util.List;

public record DiscordMessageData(
        String id,
        String channelId,
        String content,
        DiscordUserDetails author,
        List<DiscordUserDetails> mentionedUsers) {

    public DiscordMessageData {
        mentionedUsers = new ArrayList<>(mentionedUsers == null ? List.of() : mentionedUsers);
    }
}
