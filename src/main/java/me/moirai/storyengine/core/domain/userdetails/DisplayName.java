package me.moirai.storyengine.core.domain.userdetails;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Locale;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

@Embeddable
public final class DisplayName {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 32;
    private static final Set<String> RESERVED_NAMES = Set.of(
            "admin", "administrator", "moirai", "system",
            "support", "staff", "moderator", "root", "null", "undefined");

    private static final String REQUIRED = "Display name is required";
    private static final String LENGTH_INVALID = "Display name must be between 2 and 32 characters";
    private static final String RESERVED = "This name is reserved and cannot be used";

    @Column(name = "display_name")
    private String name;

    protected DisplayName() {
        super();
    }

    private DisplayName(String name) {

        if (isBlank(name)) {
            throw new BusinessRuleViolationException(REQUIRED);
        }

        if (name.length() < MIN_LENGTH || name.length() > MAX_LENGTH) {
            throw new BusinessRuleViolationException(LENGTH_INVALID);
        }

        if (RESERVED_NAMES.contains(name.trim().toLowerCase(Locale.ROOT))) {
            throw new BusinessRuleViolationException(RESERVED);
        }

        this.name = name;
    }

    public static DisplayName of(String name) {
        return new DisplayName(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {

        if (this == other) {
            return true;
        }

        if (!(other instanceof DisplayName that)) {
            return false;
        }

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
