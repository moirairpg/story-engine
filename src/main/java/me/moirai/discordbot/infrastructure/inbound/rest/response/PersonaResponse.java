package me.moirai.discordbot.infrastructure.inbound.rest.response;

import static org.apache.commons.collections4.SetUtils.emptyIfNull;

import java.time.OffsetDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonaResponse {

    private String id;
    private String name;
    private String personality;
    private String visibility;
    private String ownerDiscordId;
    private Set<String> usersAllowedToWrite;
    private Set<String> usersAllowedToRead;
    private OffsetDateTime creationDate;
    private OffsetDateTime lastUpdateDate;

    public PersonaResponse() {
    }

    private PersonaResponse(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.personality = builder.personality;
        this.visibility = builder.visibility;
        this.ownerDiscordId = builder.ownerDiscordId;
        this.usersAllowedToWrite = builder.usersAllowedToWrite;
        this.usersAllowedToRead = builder.usersAllowedToRead;
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

    public String getPersonality() {
        return personality;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getOwnerDiscordId() {
        return ownerDiscordId;
    }

    public Set<String> getUsersAllowedToWrite() {
        return usersAllowedToWrite;
    }

    public Set<String> getUsersAllowedToRead() {
        return usersAllowedToRead;
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
        private String personality;
        private String visibility;
        private String ownerDiscordId;
        private Set<String> usersAllowedToWrite;
        private Set<String> usersAllowedToRead;
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

        public Builder personality(String personality) {
            this.personality = personality;
            return this;
        }

        public Builder visibility(String visibility) {
            this.visibility = visibility;
            return this;
        }

        public Builder ownerDiscordId(String ownerDiscordId) {
            this.ownerDiscordId = ownerDiscordId;
            return this;
        }

        public Builder usersAllowedToWrite(Set<String> usersAllowedToWrite) {
            this.usersAllowedToWrite = emptyIfNull(usersAllowedToWrite);
            return this;
        }

        public Builder usersAllowedToRead(Set<String> usersAllowedToRead) {
            this.usersAllowedToRead = emptyIfNull(usersAllowedToRead);
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

        public PersonaResponse build() {
            return new PersonaResponse(this);
        }
    }
}