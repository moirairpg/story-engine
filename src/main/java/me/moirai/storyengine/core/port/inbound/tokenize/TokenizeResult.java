package me.moirai.storyengine.core.port.inbound.tokenize;

public record TokenizeResult(
        String tokens,
        int[] tokenIds,
        int tokenCount,
        int characterCount) {
}
