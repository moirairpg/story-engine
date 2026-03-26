package me.moirai.storyengine.core.port.inbound.slashcommand;

public class TokenizeResultFixture {

    public static TokenizeResult create() {

        return new TokenizeResult("This is an input.", new int[] { 1, 2, 3 }, 10, 10);
    }
}
