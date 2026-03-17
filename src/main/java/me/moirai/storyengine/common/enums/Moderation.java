package me.moirai.storyengine.common.enums;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

public enum Moderation {

    STRICT(true, null),
    PERMISSIVE(false, permissiveThresholds()),
    DISABLED(false, null);

    private final boolean isAbsolute;
    private final Map<String, Double> thresholds;

    private static final double DISABLED_TOPIC = 1.1;

    private static final String HATE = "hate";
    private static final String HATE_THREATENING = "hate/threatening";
    private static final String HARASSMENT = "harassment";
    private static final String HARASSMENT_THREATENING = "harassment/threatening";
    private static final String SELF_HARM = "self-harm";
    private static final String SELF_HARM_INTENT = "self-harm/intent";
    private static final String SELF_HARM_INSTRUCTIONS = "self-harm/instructions";
    private static final String SEXUAL = "sexual";
    private static final String SEXUAL_MINORS = "sexual/minors";
    private static final String VIOLENCE = "violence";
    private static final String VIOLENCE_GRAPHIC = "violence/graphic";

    private static Map<String, Double> permissiveThresholds() {

        Map<String, Double> thresholds = new HashMap<>();
        thresholds.put(HATE, 0.8);
        thresholds.put(HATE_THREATENING, 0.6);
        thresholds.put(HARASSMENT, DISABLED_TOPIC);
        thresholds.put(HARASSMENT_THREATENING, 0.8);
        thresholds.put(SELF_HARM, 0.3);
        thresholds.put(SELF_HARM_INTENT, 0.3);
        thresholds.put(SELF_HARM_INSTRUCTIONS, 0.3);
        thresholds.put(SEXUAL, 0.8);
        thresholds.put(SEXUAL_MINORS, 0.1);
        thresholds.put(VIOLENCE, DISABLED_TOPIC);
        thresholds.put(VIOLENCE_GRAPHIC, 0.6);

        return thresholds;
    }

    private Moderation(boolean isAbsolute, Map<String, Double> thresholds) {
        this.isAbsolute = isAbsolute;
        this.thresholds = thresholds;
    }

    public boolean isAbsolute() {
        return isAbsolute;
    }

    public Map<String, Double> getThresholds() {
        return thresholds;
    }

    public static Moderation fromString(String value) {

        if (StringUtils.isBlank(value)) {
            throw new BusinessRuleViolationException("Moderation cannot be null");
        }

        return Arrays.stream(values())
                .filter(moderation -> moderation.name().equals(value.toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleViolationException("Invalid moderation"));
    }
}
