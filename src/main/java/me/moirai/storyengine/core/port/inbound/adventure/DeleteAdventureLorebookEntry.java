package me.moirai.storyengine.core.port.inbound.adventure;

import me.moirai.storyengine.common.usecases.UseCase;

public final class DeleteAdventureLorebookEntry extends UseCase<Void> {

    private final String lorebookEntryId;
    private final String adventureId;
    private final String requesterId;

    private DeleteAdventureLorebookEntry(Builder builder) {

        this.lorebookEntryId = builder.lorebookEntryId;
        this.adventureId = builder.adventureId;
        this.requesterId = builder.requesterId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getLorebookEntryId() {
        return lorebookEntryId;
    }

    public String getAdventureId() {
        return adventureId;
    }

    public String getRequesterDiscordId() {
        return requesterId;
    }

    public static final class Builder {

        private String lorebookEntryId;
        private String adventureId;
        private String requesterId;

        private Builder() {
        }

        public Builder lorebookEntryId(String lorebookEntryId) {
            this.lorebookEntryId = lorebookEntryId;
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

        public DeleteAdventureLorebookEntry build() {
            return new DeleteAdventureLorebookEntry(this);
        }
    }
}