package me.moirai.storyengine.core.port.inbound.adventure;

import me.moirai.storyengine.common.usecases.UseCase;

public final class GetAdventureLorebookEntryById extends UseCase<AdventureLorebookEntryDetails> {

    private final String entryId;
    private final String adventureId;
    private final String requesterId;

    private GetAdventureLorebookEntryById(Builder builder) {

        this.entryId = builder.entryId;
        this.adventureId = builder.adventureId;
        this.requesterId = builder.requesterId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getEntryId() {
        return entryId;
    }

    public String getAdventureId() {
        return adventureId;
    }

    public String getRequesterDiscordId() {
        return requesterId;
    }

    public static final class Builder {

        private String entryId;
        private String adventureId;
        private String requesterId;

        private Builder() {
        }

        public Builder entryId(String entryId) {
            this.entryId = entryId;
            return this;
        }

        public Builder adventureId(String adventureId) {
            this.adventureId = adventureId;
            return this;
        }

        public Builder requesterId(String requesterId) {
            this.requesterId = requesterId;
            return this;
        }

        public GetAdventureLorebookEntryById build() {
            return new GetAdventureLorebookEntryById(this);
        }
    }
}