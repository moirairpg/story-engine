package me.moirai.storyengine.core.port.inbound.slashcommand;

public record TokenizeResult(
        String tokens,
        int[] tokenIds,
        int tokenCount,
        int characterCount) {
}
