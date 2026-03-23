package me.moirai.storyengine.core.port.inbound.discord.slashcommands;

public record TokenizeResult(
        String tokens,
        int[] tokenIds,
        int tokenCount,
        int characterCount) {
}
