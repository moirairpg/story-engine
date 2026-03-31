package me.moirai.storyengine.core.application.query.tokenize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.port.inbound.tokenize.TokenizeInput;
import me.moirai.storyengine.core.port.inbound.tokenize.TokenizeResult;
import me.moirai.storyengine.core.port.outbound.generation.TokenizerPort;

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

        var expectedAdapterResult = new TokenizeResult(tokens, tokenIds, tokenCount,
                textToBeTokenized.length());

        when(tokenizerPort.tokenize(anyString())).thenReturn(expectedAdapterResult);

        TokenizeInput useCase = new TokenizeInput(textToBeTokenized);

        TokenizeResult result = handler.execute(useCase);

        assertThat(result)
                .isNotNull()
                .isEqualTo(expectedAdapterResult);
    }
}
