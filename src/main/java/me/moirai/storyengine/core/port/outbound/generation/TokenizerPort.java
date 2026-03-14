package me.moirai.storyengine.core.port.outbound.generation;

import me.moirai.storyengine.core.port.inbound.discord.slashcommands.TokenizeResult;

public interface TokenizerPort {

    int[] getTokensIdsFrom(String text);

    int getTokenCountFrom(String text);

    String getTokens(String text);

    TokenizeResult tokenize(String text);
}
