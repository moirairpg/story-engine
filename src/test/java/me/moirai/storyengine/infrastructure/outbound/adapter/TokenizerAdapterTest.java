package me.moirai.storyengine.infrastructure.outbound.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import me.moirai.storyengine.core.application.usecase.discord.slashcommands.TokenizeResult;

public class TokenizerAdapterTest {

    private TokenizerAdapter tokenizer;

    @BeforeEach
    void setUp() {

        tokenizer = new TokenizerAdapter();
    }

    @Test
    public void extractTokenIdsFromSingleInput() {

        String textToTokenize = "This is a test.";

        int[] returnedTokenIds = tokenizer.getTokensIdsFrom(textToTokenize);

        assertThat(returnedTokenIds).isNotNull().isNotEmpty();
    }

    @Test
    public void countTokensFromSingleInput() {

        String textToTokenize = "This is a test.";
        int expectedTokenCount = 5;

        int tokenCount = tokenizer.getTokenCountFrom(textToTokenize);

        assertThat(expectedTokenCount).isEqualTo(tokenCount);
    }

    @Test
    public void countTokens_whenNullText_returnsZero() {

        String textToTokenize = null;
        int expectedTokenCount = 0;

        int tokenCount = tokenizer.getTokenCountFrom(textToTokenize);

        assertThat(expectedTokenCount).isEqualTo(tokenCount);
    }

    @Test
    public void countTokens_whenEmptyText_returnsZero() {

        String textToTokenize = "";
        int expectedTokenCount = 0;

        int tokenCount = tokenizer.getTokenCountFrom(textToTokenize);

        assertThat(expectedTokenCount).isEqualTo(tokenCount);
    }

    @Test
    public void getTokens_returnsPipeDelimitedTokenIds() {

        String textToTokenize = "Hello";

        String tokenizedText = tokenizer.getTokens(textToTokenize);

        assertThat(tokenizedText).isNotNull().isNotEmpty();
    }

    @Test
    public void tokenizeForCompleteOutput() {

        String textToTokenize = "This is a test.";
        int expectedTokenCount = 5;
        int length = textToTokenize.length();

        TokenizeResult result = tokenizer.tokenize(textToTokenize);

        assertThat(result).isNotNull();
        assertThat(result.getCharacterCount()).isEqualTo(length);
        assertThat(result.getTokenIds()).isNotNull().isNotEmpty();
        assertThat(result.getTokens()).isNotNull().isNotEmpty();
        assertThat(result.getTokenCount()).isEqualTo(expectedTokenCount);
    }
}
