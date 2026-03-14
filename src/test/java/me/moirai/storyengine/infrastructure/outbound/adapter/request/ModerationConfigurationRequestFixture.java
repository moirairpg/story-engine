package me.moirai.storyengine.infrastructure.outbound.adapter.request;

import java.util.HashMap;
import java.util.Map;

public class ModerationConfigurationRequestFixture {

    public static ModerationConfigurationRequest absoluteWithFlags() {

        Map<String, Double> thresholds = new HashMap<>();
        thresholds.put("sexual", 1.0);
        thresholds.put("violence", 1.0);

        return ModerationConfigurationRequest.build(true, true, thresholds);
    }

    public static ModerationConfigurationRequest withFlags() {

        Map<String, Double> thresholds = new HashMap<>();
        thresholds.put("sexual", 1.0);
        thresholds.put("violence", 1.0);

        return ModerationConfigurationRequest.build(true, false, thresholds);
    }

    public static ModerationConfigurationRequest disabled() {

        Map<String, Double> thresholds = new HashMap<>();

        return ModerationConfigurationRequest.build(false, false, thresholds);
    }
}
