package me.moirai.storyengine.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

@Embeddable
public record Narrator(
        @Column(name = "narrator_name") String narratorName,
        @Column(name = "narrator_personality") String narratorPersonality) {

    public Narrator {
        if (narratorPersonality == null && narratorName != null) {
            throw new BusinessRuleViolationException(
                    "Narrator name cannot be set without a personality");
        }

        if (narratorPersonality != null && narratorName == null) {
            narratorName = "Narrator";
        }
    }
}
