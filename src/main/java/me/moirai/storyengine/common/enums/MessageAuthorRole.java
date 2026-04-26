package me.moirai.storyengine.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MessageAuthorRole {

        SYSTEM,
        ASSISTANT,
        USER;

        @JsonValue
        public String toJsonValue() {
            return name().toLowerCase();
        }

        @JsonCreator
        public static MessageAuthorRole fromValue(String value) {
            return valueOf(value.toUpperCase());
        }
    }
