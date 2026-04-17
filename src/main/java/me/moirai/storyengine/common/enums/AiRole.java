package me.moirai.storyengine.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AiRole {

        SYSTEM,
        ASSISTANT,
        USER;

        @JsonValue
        public String toJsonValue() {
            return name().toLowerCase();
        }

        @JsonCreator
        public static AiRole fromValue(String value) {
            return valueOf(value.toUpperCase());
        }
    }
