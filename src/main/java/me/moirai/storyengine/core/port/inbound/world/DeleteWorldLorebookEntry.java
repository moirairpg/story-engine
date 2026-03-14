package me.moirai.storyengine.core.port.inbound.world;

import me.moirai.storyengine.common.usecases.UseCase;

public final class DeleteWorldLorebookEntry extends UseCase<Void> {

    private final String lorebookEntryId;
    private final String worldId;
    private final String requesterId;

    private DeleteWorldLorebookEntry(Builder builder) {

        this.lorebookEntryId = builder.lorebookEntryId;
        this.worldId = builder.worldId;
        this.requesterId = builder.requesterId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getLorebookEntryId() {
        return lorebookEntryId;
    }

    public String getWorldId() {
        return worldId;
    }

    public String getRequesterDiscordId() {
        return requesterId;
    }

    public static final class Builder {

        private String lorebookEntryId;
        private String worldId;
        private String requesterId;

        private Builder() {
        }

        public Builder lorebookEntryId(String lorebookEntryId) {
            this.lorebookEntryId = lorebookEntryId;
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

        public DeleteWorldLorebookEntry build() {
            return new DeleteWorldLorebookEntry(this);
        }
    }
}