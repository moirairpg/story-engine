package me.moirai.storyengine.core.application.usecase.adventure.request;

import me.moirai.storyengine.common.usecases.UseCase;

public final class UpdateAdventureAuthorsNoteByChannelId extends UseCase<Void> {

    private final String authorsNote;
    private final String channelId;
    private final String requesterId;

    private UpdateAdventureAuthorsNoteByChannelId(Builder builder) {
        this.authorsNote = builder.authorsNote;
        this.channelId = builder.channelId;
        this.requesterId = builder.requesterId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getAuthorsNote() {
        return authorsNote;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getRequesterDiscordId() {
        return requesterId;
    }

    public static final class Builder {

        private String authorsNote;
        private String channelId;
        private String requesterId;

        public Builder authorsNote(String authorsNote) {
            this.authorsNote = authorsNote;
            return this;
        }

        public Builder channelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder requesterId(String requesterId) {
            this.requesterId = requesterId;
            return this;
        }

        public UpdateAdventureAuthorsNoteByChannelId build() {
            return new UpdateAdventureAuthorsNoteByChannelId(this);
        }
    }
}