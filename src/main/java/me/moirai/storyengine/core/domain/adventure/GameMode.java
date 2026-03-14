package me.moirai.storyengine.core.domain.adventure;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

public enum GameMode {

    CHAT,
    RPG,
    AUTHOR;

    public static GameMode fromString(String value) {

        if (StringUtils.isBlank(value)) {
            throw new BusinessRuleViolationException("Game Mode cannot be null");
        }

        return Arrays.stream(values())
                .filter(gameMode -> gameMode.name().equals(value.toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleViolationException("Invalid game mode"));
    }
}
