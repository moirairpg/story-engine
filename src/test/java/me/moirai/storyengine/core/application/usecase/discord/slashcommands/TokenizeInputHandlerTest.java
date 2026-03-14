package me.moirai.storyengine.core.application.usecase.discord.slashcommands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.domain.port.TokenizerPort;

@ExtendWith(MockitoExtension.class)
public class TokenizeInputHandlerTest {

    @Mock
    private TokenizerPort tokenizerPort;

    @InjectMocks
    private TokenizeInputHandler handler;

    @Test
    public void tokenizeCommand_whenInputIsSupplied_thenReturnTokenizedOutput() {

        String textToBeTokenized = "This is some text.";
        String tokens = "1212|318|617|2420|13";
        int[] tokenIds = { 1212, 318, 617, 2420, 13 };
        int tokenCount = 5;

        TokenizeResult expectedAdapterResult = TokenizeResult.builder()
                .tokens(tokens)
                .tokenCount(tokenCount)
                .tokenIds(tokenIds)
                .characterCount(textToBeTokenized.length())
                .build();

        when(tokenizerPort.tokenize(anyString())).thenReturn(expectedAdapterResult);

        TokenizeInput useCase = TokenizeInput.build(textToBeTokenized);

        Optional<TokenizeResult> result = handler.execute(useCase);

        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .contains(expectedAdapterResult);
    }

    @Test
    public void tokenizeCommand_whenEncodingErrorIsThrown_thenReturnErrorOutput() {

        String textToBeTokenized = "This is some text.";

        TokenizeInput useCase = TokenizeInput.build(textToBeTokenized);

        Optional<TokenizeResult> result = handler.execute(useCase);

        assertThat(result)
                .isNotNull()
                .isEmpty();
    }
}
