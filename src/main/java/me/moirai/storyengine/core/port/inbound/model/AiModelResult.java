package me.moirai.storyengine.core.port.inbound.model;

public record AiModelResult(
        String fullModelName,
        String internalModelName,
        String officialModelName,
        int hardTokenLimit) {
}
