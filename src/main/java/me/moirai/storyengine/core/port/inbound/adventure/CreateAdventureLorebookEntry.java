package me.moirai.storyengine.core.port.inbound.adventure;

import me.moirai.storyengine.common.usecases.UseCase;
import reactor.core.publisher.Mono;

public final class CreateAdventureLorebookEntry extends UseCase<Mono<CreateAdventureLorebookEntryResult>> {

    private final String adventureId;
    private final String name;
    private final String regex;
    private final String description;
    private final String playerId;
    private final String requesterId;

    private CreateAdventureLorebookEntry(Builder builder) {

        this.adventureId = builder.adventureId;
        this.name = builder.name;
        this.regex = builder.regex;
        this.description = builder.description;
        this.playerId = builder.playerId;
        this.requesterId = builder.requesterId;
    }

    public static Builder builder() {
        return new Builder();
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

    public String getRequesterDiscordId() {
        return requesterId;
    }

    public static final class Builder {

        private String adventureId;
        private String name;
        private String regex;
        private String description;
        private String playerId;
        private String requesterId;

        private Builder() {
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

        public Builder requesterId(String requesterId) {
            this.requesterId = requesterId;
            return this;
        }

        public CreateAdventureLorebookEntry build() {
            return new CreateAdventureLorebookEntry(this);
        }
    }
}