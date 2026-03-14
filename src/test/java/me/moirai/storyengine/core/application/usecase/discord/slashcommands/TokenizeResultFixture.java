package me.moirai.storyengine.core.application.usecase.discord.slashcommands;

import me.moirai.storyengine.core.port.inbound.discord.slashcommands.TokenizeResult;

public class TokenizeResultFixture {

    public static TokenizeResult.Builder create() {

        return TokenizeResult.builder()
                .characterCount(10)
                .tokenCount(10)
                .tokenIds(new int[] { 1, 2, 3 })
                .tokens("This is an input.");
    }
}
