package me.moirai.storyengine.core.port.outbound.generation;

public class TextGenerationResultFixture {

    public static TextGenerationResult.Builder create() {

        return TextGenerationResult.builder()
                .completionTokens(1024)
                .promptTokens(1024)
                .totalTokens(2048)
                .outputText("This is the output");
    }
}
