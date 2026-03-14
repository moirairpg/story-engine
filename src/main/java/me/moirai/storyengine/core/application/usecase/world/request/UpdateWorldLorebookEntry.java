package me.moirai.storyengine.core.application.usecase.world.request;

import me.moirai.storyengine.common.usecases.UseCase;
import me.moirai.storyengine.core.application.usecase.world.result.UpdateWorldLorebookEntryResult;

public final class UpdateWorldLorebookEntry extends UseCase<UpdateWorldLorebookEntryResult> {

    private final String id;
    private final String worldId;
    private final String name;
    private final String regex;
    private final String description;
    private final String requesterId;

    private UpdateWorldLorebookEntry(Builder builder) {

        this.id = builder.id;
        this.worldId = builder.worldId;
        this.name = builder.name;
        this.regex = builder.regex;
        this.description = builder.description;
        this.requesterId = builder.requesterId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
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

        private String id;
        private String worldId;
        private String name;
        private String regex;
        private String description;
        private String requesterId;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
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

        public UpdateWorldLorebookEntry build() {
            return new UpdateWorldLorebookEntry(this);
        }
    }
}