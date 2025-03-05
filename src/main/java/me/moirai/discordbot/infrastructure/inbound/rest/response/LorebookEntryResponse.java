package me.moirai.discordbot.infrastructure.inbound.rest.response;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LorebookEntryResponse {

    private String id;
    private String name;
    private String regex;
    private String description;
    private String playerDiscordId;
    private boolean isPlayerCharacter;
    private OffsetDateTime creationDate;
    private OffsetDateTime lastUpdateDate;

    public LorebookEntryResponse() {
    }

    private LorebookEntryResponse(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.regex = builder.regex;
        this.description = builder.description;
        this.playerDiscordId = builder.playerDiscordId;
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

    public String getPlayerDiscordId() {
        return playerDiscordId;
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

    public static final class Builder {
        private String id;
        private String name;
        private String regex;
        private String description;
        private String playerDiscordId;
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

        public Builder playerDiscordId(String playerDiscordId) {
            this.playerDiscordId = playerDiscordId;
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

        public LorebookEntryResponse build() {
            return new LorebookEntryResponse(this);
        }
    }
}