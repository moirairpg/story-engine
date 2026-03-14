package me.moirai.storyengine.core.port.inbound.world;

import static java.util.Collections.unmodifiableSet;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = WorldDetails.Builder.class)
public final class WorldDetails {

    private final String id;
    private final String name;
    private final String description;
    private final String adventureStart;
    private final String visibility;
    private final String ownerId;
    private final Set<String> usersAllowedToRead;
    private final Set<String> usersAllowedToWrite;
    private final OffsetDateTime creationDate;
    private final OffsetDateTime lastUpdateDate;

    private WorldDetails(Builder builder) {

        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.adventureStart = builder.adventureStart;
        this.visibility = builder.visibility;
        this.ownerId = builder.ownerId;
        this.usersAllowedToRead = unmodifiableSet(builder.usersAllowedToRead);
        this.usersAllowedToWrite = unmodifiableSet(builder.usersAllowedToWrite);
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

    public String getDescription() {
        return description;
    }

    public String getAdventureStart() {
        return adventureStart;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public Set<String> getUsersAllowedToRead() {
        return usersAllowedToRead;
    }

    public Set<String> getUsersAllowedToWrite() {
        return usersAllowedToWrite;
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
        private String description;
        private String adventureStart;
        private String visibility;
        private String ownerId;
        private Set<String> usersAllowedToRead = new HashSet<>();
        private Set<String> usersAllowedToWrite = new HashSet<>();
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

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder adventureStart(String adventureStart) {
            this.adventureStart = adventureStart;
            return this;
        }

        public Builder visibility(String visibility) {
            this.visibility = visibility;
            return this;
        }

        public Builder ownerId(String ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public Builder usersAllowedToRead(Set<String> usersAllowedToRead) {

            if (usersAllowedToRead != null) {
                this.usersAllowedToRead = usersAllowedToRead;
            }

            return this;
        }

        public Builder usersAllowedToWrite(Set<String> usersAllowedToWrite) {

            if (usersAllowedToWrite != null) {
                this.usersAllowedToWrite = usersAllowedToWrite;
            }

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

        public WorldDetails build() {
            return new WorldDetails(this);
        }
    }
}
