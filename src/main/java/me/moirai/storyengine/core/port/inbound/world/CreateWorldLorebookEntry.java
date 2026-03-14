package me.moirai.storyengine.core.port.inbound.world;

import me.moirai.storyengine.common.usecases.UseCase;

public final class CreateWorldLorebookEntry extends UseCase<CreateWorldLorebookEntryResult> {

    private final String worldId;
    private final String name;
    private final String regex;
    private final String description;
    private final String requesterId;

    private CreateWorldLorebookEntry(Builder builder) {

        this.worldId = builder.worldId;
        this.name = builder.name;
        this.regex = builder.regex;
        this.description = builder.description;
        this.requesterId = builder.requesterId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getWorldId() {
        return worldId;
    }

    public String getName() {
        return name;
    }

    public String getRegex() {
        return regex;
    }

    public String getDescription() {
        return description;
    }

    public String getRequesterDiscordId() {
        return requesterId;
    }

    public static final class Builder {

        private String worldId;
        private String name;
        private String regex;
        private String description;
        private String requesterId;

        private Builder() {
        }

        public Builder worldId(String worldId) {
            this.worldId = worldId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder regex(String regex) {
            this.regex = regex;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder requesterId(String requesterId) {
            this.requesterId = requesterId;
            return this;
        }

        public CreateWorldLorebookEntry build() {
            return new CreateWorldLorebookEntry(this);
        }
    }
}