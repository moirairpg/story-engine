package me.moirai.storyengine.core.domain.adventure;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public final class ContextAttributes {

    @Column(name = "nudge")
    private String nudge;

    @Column(name = "authors_note")
    private String authorsNote;

    @Column(name = "remember")
    private String remember;

    @Column(name = "bump")
    private String bump;

    @Column(name = "bump_frequency")
    private Integer bumpFrequency;

    public ContextAttributes(Builder builder) {
        this.nudge = builder.nudge;
        this.authorsNote = builder.authorsNote;
        this.remember = builder.remember;
        this.bump = builder.bump;
        this.bumpFrequency = builder.bumpFrequency;
    }

    protected ContextAttributes() {
        super();
    }

    public static Builder builder() {
        return new Builder();
    }

    private Builder cloneFrom(ContextAttributes contextAttributes) {

        return builder()
                .nudge(contextAttributes.getNudge())
                .bump(contextAttributes.getBump())
                .bumpFrequency(contextAttributes.getBumpFrequency())
                .authorsNote(contextAttributes.getAuthorsNote())
                .remember(contextAttributes.getRemember());
    }

    public String getNudge() {
        return nudge;
    }

    public String getAuthorsNote() {
        return authorsNote;
    }

    public String getRemember() {
        return remember;
    }

    public String getBump() {
        return bump;
    }

    public Integer getBumpFrequency() {
        return bumpFrequency;
    }

    public ContextAttributes updateNudge(String nudge) {

        return cloneFrom(this).nudge(nudge).build();
    }

    public ContextAttributes updateBump(String bump) {

        return cloneFrom(this).bump(bump).build();
    }

    public ContextAttributes updateBumpFrequency(int bumpFrequency) {

        return cloneFrom(this).bumpFrequency(bumpFrequency).build();
    }

    public ContextAttributes updateAuthorsNote(String authorsNote) {

        return cloneFrom(this).authorsNote(authorsNote).build();
    }

    public ContextAttributes updateRemember(String remember) {

        return cloneFrom(this).remember(remember).build();
    }

    public static final class Builder {

        private String nudge;
        private String authorsNote;
        private String remember;
        private String bump;
        private Integer bumpFrequency;

        private Builder() {
        }

        public Builder nudge(String nudge) {
            this.nudge = nudge;
            return this;
        }

        public Builder authorsNote(String authorsNote) {
            this.authorsNote = authorsNote;
            return this;
        }

        public Builder remember(String remember) {
            this.remember = remember;
            return this;
        }

        public Builder bump(String bump) {
            this.bump = bump;
            return this;
        }

        public Builder bumpFrequency(Integer bumpFrequency) {
            this.bumpFrequency = bumpFrequency;
            return this;
        }

        public ContextAttributes build() {
            return new ContextAttributes(this);
        }
    }
}
