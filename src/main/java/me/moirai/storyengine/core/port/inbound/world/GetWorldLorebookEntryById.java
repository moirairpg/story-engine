package me.moirai.storyengine.core.port.inbound.world;

import me.moirai.storyengine.common.usecases.UseCase;

public final class GetWorldLorebookEntryById extends UseCase<GetWorldLorebookEntryResult> {

    private final String entryId;
    private final String worldId;
    private final String requesterId;

    private GetWorldLorebookEntryById(Builder builder) {

        this.entryId = builder.entryId;
        this.worldId = builder.worldId;
        this.requesterId = builder.requesterId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getEntryId() {
        return entryId;
    }

    public String getWorldId() {
        return worldId;
    }

    public String getRequesterDiscordId() {
        return requesterId;
    }

    public static final class Builder {

        private String entryId;
        private String worldId;
        private String requesterId;

        private Builder() {
        }

        public Builder entryId(String entryId) {
            this.entryId = entryId;
            return this;
        }

        public Builder worldId(String worldId) {
            this.worldId = worldId;
            return this;
        }

        public Builder requesterId(String requesterId) {
            this.requesterId = requesterId;
            return this;
        }

        public GetWorldLorebookEntryById build() {
            return new GetWorldLorebookEntryById(this);
        }
    }
}