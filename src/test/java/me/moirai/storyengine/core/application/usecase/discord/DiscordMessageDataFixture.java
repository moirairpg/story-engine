package me.moirai.storyengine.core.application.usecase.discord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DiscordMessageDataFixture {

    public static DiscordMessageData.Builder messageData() {

        return DiscordMessageData.builder()
                .id("2")
                .channelId("12345")
                .content("Some message")
                .author(DiscordUserDetailsFixture.create().build());
    }

    public static List<DiscordMessageData> messageList(int amountOfMessages) {

        DiscordUserDetails author = DiscordUserDetailsFixture.create().build();
        return IntStream.range(0, amountOfMessages)
                .mapToObj(index -> messageData()
                        .id(String.valueOf(index + 1))
                        .content(String.format("%s said: Message %s", author.getNickname(), index + 1))
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
