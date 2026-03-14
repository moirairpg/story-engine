package me.moirai.storyengine.core.application.usecase.adventure.request;

import me.moirai.storyengine.common.usecases.UseCase;

public final class UpdateAdventureRememberByChannelId extends UseCase<Void> {

    private final String remember;
    private final String channelId;
    private final String requesterId;

    private UpdateAdventureRememberByChannelId(Builder builder) {
        this.remember = builder.remember;
        this.channelId = builder.channelId;
        this.requesterId = builder.requesterId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getRemember() {
        return remember;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getRequesterDiscordId() {
        return requesterId;
    }

    public static final class Builder {

        private String remember;
        private String channelId;
        private String requesterId;

        public Builder remember(String remember) {
            this.remember = remember;
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

        public UpdateAdventureRememberByChannelId build() {
            return new UpdateAdventureRememberByChannelId(this);
        }
    }
}