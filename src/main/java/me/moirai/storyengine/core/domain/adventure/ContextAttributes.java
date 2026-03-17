package me.moirai.storyengine.core.domain.adventure;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record ContextAttributes(
        @Column(name = "nudge") String nudge,
        @Column(name = "authors_note") String authorsNote,
        @Column(name = "remember") String remember,
        @Column(name = "bump") String bump,
        @Column(name = "bump_frequency") Integer bumpFrequency) {

    public ContextAttributes updateNudge(String nudge) {

        return new ContextAttributes(nudge, authorsNote, remember, bump, bumpFrequency);
    }

    public ContextAttributes updateBump(String bump) {

        return new ContextAttributes(nudge, authorsNote, remember, bump, bumpFrequency);
    }

    public ContextAttributes updateBumpFrequency(int bumpFrequency) {

        return new ContextAttributes(nudge, authorsNote, remember, bump, bumpFrequency);
    }

    public ContextAttributes updateAuthorsNote(String authorsNote) {

        return new ContextAttributes(nudge, authorsNote, remember, bump, bumpFrequency);
    }

    public ContextAttributes updateRemember(String remember) {

        return new ContextAttributes(nudge, authorsNote, remember, bump, bumpFrequency);
    }
}
