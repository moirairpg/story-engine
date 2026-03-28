package me.moirai.storyengine.core.port.outbound.generation;

public record ModelConfigurationRequest(
        AiModelRequest aiModel,
        Integer maxTokenLimit,
        Double temperature) {
}
