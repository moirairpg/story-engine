package me.moirai.storyengine.core.port.outbound;

import java.util.Map;

public class ModerationConfigurationRequest {

    private final boolean isEnabled;
    private final boolean isAbsolute;
    private final Map<String, Double> thresholds;

    private ModerationConfigurationRequest(boolean isEnabled, boolean isAbsolute, Map<String, Double> thresholds) {
        this.isEnabled = isEnabled;
        this.isAbsolute = isAbsolute;
        this.thresholds = thresholds;
    }

    public static ModerationConfigurationRequest build(boolean isEnabled, boolean isAbsolute, Map<String, Double> thresholds) {
        return new ModerationConfigurationRequest(isEnabled, isAbsolute, thresholds);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isAbsolute() {
        return isAbsolute;
    }

    public Map<String, Double> getThresholds() {
        return thresholds;
    }
}
