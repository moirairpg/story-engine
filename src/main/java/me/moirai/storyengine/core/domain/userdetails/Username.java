package me.moirai.storyengine.core.domain.userdetails;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Locale;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

@Embeddable
public final class Username {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 32;
    private static final String ALLOWED_CHARACTERS = "^[a-zA-Z0-9_.]+$";
    private static final String CONSECUTIVE_PERIODS = "..";
    private static final Set<String> RESERVED_NAMES = Set.of(
            "admin", "administrator", "moirai", "system",
            "support", "staff", "moderator", "root", "null", "undefined");

    private static final String REQUIRED = "Username is required";
    private static final String LENGTH_INVALID = "Username must be between 2 and 32 characters";
    private static final String CHARSET_INVALID = "Username may only contain letters, digits, underscores and periods";
    private static final String PERIODS_INVALID = "Username cannot contain two consecutive periods";
    private static final String RESERVED = "This name is reserved and cannot be used";

    @Column(name = "username")
    private String name;

    protected Username() {
        super();
    }

    private Username(String name) {

        if (isBlank(name)) {
            throw new BusinessRuleViolationException(REQUIRED);
        }

        if (name.length() < MIN_LENGTH || name.length() > MAX_LENGTH) {
            throw new BusinessRuleViolationException(LENGTH_INVALID);
        }

        if (!name.matches(ALLOWED_CHARACTERS)) {
            throw new BusinessRuleViolationException(CHARSET_INVALID);
        }

        if (name.contains(CONSECUTIVE_PERIODS)) {
            throw new BusinessRuleViolationException(PERIODS_INVALID);
        }

        if (RESERVED_NAMES.contains(name.toLowerCase(Locale.ROOT))) {
            throw new BusinessRuleViolationException(RESERVED);
        }

        this.name = name;
    }

    public static Username of(String name) {
        return new Username(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {

        if (this == other) {
            return true;
        }

        if (!(other instanceof Username that)) {
            return false;
        }

        return name.toLowerCase(Locale.ROOT).equals(that.name.toLowerCase(Locale.ROOT));
    }

    @Override
    public int hashCode() {
        return name.toLowerCase(Locale.ROOT).hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
