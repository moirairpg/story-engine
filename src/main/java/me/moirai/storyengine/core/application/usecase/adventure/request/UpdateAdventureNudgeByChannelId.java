package me.moirai.storyengine.core.application.usecase.adventure.request;

import me.moirai.storyengine.common.usecases.UseCase;

public final class UpdateAdventureNudgeByChannelId extends UseCase<Void> {

    private final String nudge;
    private final String channelId;
    private final String requesterId;

    private UpdateAdventureNudgeByChannelId(Builder builder) {
        this.nudge = builder.nudge;
        this.channelId = builder.channelId;
        this.requesterId = builder.requesterId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getNudge() {
        return nudge;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getRequesterDiscordId() {
        return requesterId;
    }

    public static final class Builder {

        private String nudge;
        private String channelId;
        private String requesterId;

        public Builder nudge(String nudge) {
            this.nudge = nudge;
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

        public UpdateAdventureNudgeByChannelId build() {
            return new UpdateAdventureNudgeByChannelId(this);
        }
    }
}