package me.moirai.storyengine.core.domain;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

public enum CompletionRole {

    USER,
    ASSISTANT,
    SYSTEM;

    public static CompletionRole fromString(String value) {

        if (StringUtils.isBlank(value)) {
            throw new BusinessRuleViolationException("Completion role cannot be null");
        }

        return Arrays.stream(values())
                .filter(role -> role.name().equals(value.toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleViolationException("Invalid AI completion role"));
    }
}
