package me.moirai.storyengine.core.port.inbound.adventure;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = GetAdventureLorebookEntryResult.Builder.class)
public final class GetAdventureLorebookEntryResult {

    private final String id;
    private final String name;
    private final String regex;
    private final String description;
    private final String playerId;
    private final boolean isPlayerCharacter;
    private final OffsetDateTime creationDate;
    private final OffsetDateTime lastUpdateDate;

    private GetAdventureLorebookEntryResult(Builder builder) {

        this.id = builder.id;
        this.name = builder.name;
        this.regex = builder.regex;
        this.description = builder.description;
        this.playerId = builder.playerId;
        this.isPlayerCharacter = builder.isPlayerCharacter;
        this.creationDate = builder.creationDate;
        this.lastUpdateDate = builder.lastUpdateDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
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

    public OffsetDateTime getCreationDate() {
        return creationDate;
    }

    public OffsetDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {

        private String id;
        private String name;
        private String regex;
        private String description;
        private String playerId;
        private boolean isPlayerCharacter;
        private OffsetDateTime creationDate;
        private OffsetDateTime lastUpdateDate;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
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

        public Builder creationDate(OffsetDateTime creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public Builder lastUpdateDate(OffsetDateTime lastUpdateDate) {
            this.lastUpdateDate = lastUpdateDate;
            return this;
        }

        public GetAdventureLorebookEntryResult build() {
            return new GetAdventureLorebookEntryResult(this);
        }
    }
}