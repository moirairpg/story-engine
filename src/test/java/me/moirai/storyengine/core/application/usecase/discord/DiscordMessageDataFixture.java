package me.moirai.storyengine.core.application.usecase.discord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import me.moirai.storyengine.core.port.outbound.discord.DiscordMessageData;

public class DiscordMessageDataFixture {

    public static DiscordMessageData messageData() {

        return new DiscordMessageData("2", "12345", "Some message",
                DiscordUserDetailsFixture.create().build(), List.of());
    }

    public static List<DiscordMessageData> messageList(int amountOfMessages) {

        var author = DiscordUserDetailsFixture.create().build();
        return IntStream.range(0, amountOfMessages)
                .mapToObj(index -> new DiscordMessageData(
                        String.valueOf(index + 1),
                        "12345",
                        String.format("%s said: Message %s", author.getNickname(), index + 1),
                        author,
                        List.of()))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
