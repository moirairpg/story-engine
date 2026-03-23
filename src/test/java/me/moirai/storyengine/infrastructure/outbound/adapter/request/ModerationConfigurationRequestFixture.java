package me.moirai.storyengine.infrastructure.outbound.adapter.request;

import java.util.Map;

import me.moirai.storyengine.core.port.outbound.generation.ModerationConfigurationRequest;

public class ModerationConfigurationRequestFixture {

    public static ModerationConfigurationRequest absoluteWithFlags() {

        return new ModerationConfigurationRequest(true, true,
                Map.of("sexual", 1.0, "violence", 1.0));
    }

    public static ModerationConfigurationRequest withFlags() {

        return new ModerationConfigurationRequest(true, false,
                Map.of("sexual", 1.0, "violence", 1.0));
    }

    public static ModerationConfigurationRequest disabled() {

        return new ModerationConfigurationRequest(false, false, Map.of());
    }
}
