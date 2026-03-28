package me.moirai.storyengine.core.port.inbound.adventure;

import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;

public record ModelConfigurationDto(
        ArtificialIntelligenceModel aiModel,
        Integer maxTokenLimit,
        Double temperature) {
}
