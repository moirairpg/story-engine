package me.moirai.storyengine.core.domain.port;

import me.moirai.storyengine.core.application.usecase.discord.slashcommands.TokenizeResult;

public interface TokenizerPort {

    int[] getTokensIdsFrom(String text);

    int getTokenCountFrom(String text);

    String getTokens(String text);

    TokenizeResult tokenize(String text);
}
