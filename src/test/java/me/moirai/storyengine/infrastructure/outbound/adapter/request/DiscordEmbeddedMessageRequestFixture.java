package me.moirai.storyengine.infrastructure.outbound.adapter.request;

import me.moirai.storyengine.core.port.outbound.discord.DiscordEmbeddedMessageRequest;
import me.moirai.storyengine.core.port.outbound.discord.DiscordEmbeddedMessageRequest.Color;

public class DiscordEmbeddedMessageRequestFixture {

    public static DiscordEmbeddedMessageRequest.Builder create() {

        return DiscordEmbeddedMessageRequest.builder()
                .authorIconUrl("https://icon.url/icon.jpg")
                .authorName("Author")
                .authorWebsiteUrl("https://author.com")
                .embedColor(Color.YELLOW)
                .footerText("Footer")
                .imageUrl("https://icon.url/icon.jpg")
                .messageContent("Some text")
                .thumbnailUrl("https://icon.url/icon.jpg")
                .titleText("Title");
    }
}
