package me.moirai.storyengine.common.domain;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

public enum Visibility {

    PUBLIC,
    PRIVATE;

    public static Visibility fromString(String value) {

        if (StringUtils.isBlank(value)) {
            throw new BusinessRuleViolationException("Visibility cannot be null");
        }

        return Arrays.stream(values())
                .filter(visibility -> visibility.name().equals(value.toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleViolationException("Invalid visibility"));
    }
}
