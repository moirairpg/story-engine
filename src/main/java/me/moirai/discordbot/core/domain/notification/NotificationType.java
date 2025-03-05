package me.moirai.discordbot.core.domain.notification;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import me.moirai.discordbot.common.exception.BusinessRuleViolationException;

public enum NotificationType {

    INFO,
    WARNING,
    URGENT;

    public static NotificationType fromString(String value) {

        if (StringUtils.isBlank(value)) {
            throw new BusinessRuleViolationException("Notification yype cannot be null");
        }

        return Arrays.stream(values())
                .filter(moderation -> moderation.name().equals(value.toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleViolationException("Invalid notification type"));
    }
}
