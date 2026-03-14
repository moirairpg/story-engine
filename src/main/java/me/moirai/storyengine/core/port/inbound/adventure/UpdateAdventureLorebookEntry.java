package me.moirai.storyengine.core.port.inbound.adventure;

import me.moirai.storyengine.common.usecases.UseCase;
import reactor.core.publisher.Mono;

public final class UpdateAdventureLorebookEntry extends UseCase<Mono<AdventureLorebookEntryDetails>> {

    private final String id;
    private final String adventureId;
    private final String name;
    private final String regex;
    private final String description;
    private final String playerId;
    private final boolean isPlayerCharacter;
    private final String requesterId;

    private UpdateAdventureLorebookEntry(Builder builder) {

        this.id = builder.id;
        this.adventureId = builder.adventureId;
        this.name = builder.name;
        this.regex = builder.regex;
        this.description = builder.description;
        this.playerId = builder.playerId;
        this.isPlayerCharacter = builder.isPlayerCharacter;
        this.requesterId = builder.requesterId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public String getAdventureId() {
        return adventureId;
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

    public String getPlayerId() {
        return playerId;
    }

    public boolean isPlayerCharacter() {
        return isPlayerCharacter;
    }

    public String getRequesterDiscordId() {
        return requesterId;
    }

    public static final class Builder {

        private String id;
        private String adventureId;
        private String name;
        private String regex;
        private String description;
        private String playerId;
        private boolean isPlayerCharacter;
        private String requesterId;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder adventureId(String adventureId) {
            this.adventureId = adventureId;
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

        public Builder playerId(String playerId) {
            this.playerId = playerId;
            return this;
        }

        public Builder isPlayerCharacter(boolean isPlayerCharacter) {
            this.isPlayerCharacter = isPlayerCharacter;
            return this;
        }

        public Builder requesterId(String requesterId) {
            this.requesterId = requesterId;
            return this;
        }

        public UpdateAdventureLorebookEntry build() {
            return new UpdateAdventureLorebookEntry(this);
        }
    }
}