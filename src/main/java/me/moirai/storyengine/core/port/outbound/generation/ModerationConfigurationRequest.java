package me.moirai.storyengine.core.port.outbound.generation;

import java.util.Map;

public record ModerationConfigurationRequest(

        boolean isEnabled,
        boolean isAbsolute,
        Map<String, Double> thresholds) {

    public ModerationConfigurationRequest {
        thresholds = Map.copyOf(thresholds == null ? Map.of() : thresholds);
    }
}
