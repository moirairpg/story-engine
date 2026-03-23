package me.moirai.storyengine.core.application.usecase.discord.slashcommands;

import me.moirai.storyengine.core.port.inbound.discord.slashcommands.TokenizeResult;

public class TokenizeResultFixture {

    public static TokenizeResult create() {

        return new TokenizeResult("This is an input.", new int[] { 1, 2, 3 }, 10, 10);
    }
}
