package me.moirai.storyengine.core.port.outbound.generation;

public record AiModelRequest(
        String internalModelName,
        String officialModelName,
        int hardTokenLimit) {
}
