package me.moirai.storyengine.core.port.inbound.adventure;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = GetAdventureResult.Builder.class)
public final class GetAdventureResult {

    private final String id;
    private final String name;
    private final String description;
    private final String adventureStart;
    private final String worldId;
    private final String personaId;
    private final String channelId;
    private final String visibility;
    private final String aiModel;
    private final String moderation;
    private final String gameMode;
    private final String ownerId;
    private final String nudge;
    private final String remember;
    private final String authorsNote;
    private final String bump;
    private final int bumpFrequency;
    private final int maxTokenLimit;
    private final double temperature;
    private final double frequencyPenalty;
    private final double presencePenalty;
    private final boolean isMultiplayer;
    private final OffsetDateTime creationDate;
    private final OffsetDateTime lastUpdateDate;
    private final Map<String, Double> logitBias;
    private final Set<String> stopSequences;
    private final Set<String> usersAllowedToRead;
    private final Set<String> usersAllowedToWrite;

    private GetAdventureResult(Builder builder) {

        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.adventureStart = builder.adventureStart;
        this.worldId = builder.worldId;
        this.personaId = builder.personaId;
        this.channelId = builder.channelId;
        this.visibility = builder.visibility;
        this.aiModel = builder.aiModel;
        this.moderation = builder.moderation;
        this.gameMode = builder.gameMode;
        this.isMultiplayer = builder.isMultiplayer;
        this.maxTokenLimit = builder.maxTokenLimit;
        this.temperature = builder.temperature;
        this.frequencyPenalty = builder.frequencyPenalty;
        this.presencePenalty = builder.presencePenalty;
        this.ownerId = builder.ownerId;
        this.creationDate = builder.creationDate;
        this.lastUpdateDate = builder.lastUpdateDate;
        this.nudge = builder.nudge;
        this.remember = builder.remember;
        this.authorsNote = builder.authorsNote;
        this.bump = builder.bump;
        this.bumpFrequency = builder.bumpFrequency;
        this.logitBias = unmodifiableMap(builder.logitBias);
        this.stopSequences = unmodifiableSet(builder.stopSequences);
        this.usersAllowedToRead = unmodifiableSet(builder.usersAllowedToRead);
        this.usersAllowedToWrite = unmodifiableSet(builder.usersAllowedToWrite);
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

    public String getWorldId() {
        return worldId;
    }

    public String getPersonaId() {
        return personaId;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getAiModel() {
        return aiModel;
    }

    public String getModeration() {
        return moderation;
    }

    public String getGameMode() {
        return gameMode;
    }

    public boolean isMultiplayer() {
        return isMultiplayer;
    }

    public int getMaxTokenLimit() {
        return maxTokenLimit;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getFrequencyPenalty() {
        return frequencyPenalty;
    }

    public double getPresencePenalty() {
        return presencePenalty;
    }

    public String getDescription() {
        return description;
    }

    public String getAdventureStart() {
        return adventureStart;
    }

    public Set<String> getStopSequences() {
        return stopSequences;
    }

    public Map<String, Double> getLogitBias() {
        return logitBias;
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

    public String getNudge() {
        return nudge;
    }

    public String getRemember() {
        return remember;
    }

    public String getAuthorsNote() {
        return authorsNote;
    }

    public String getBump() {
        return bump;
    }

    public int getBumpFrequency() {
        return bumpFrequency;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {

        private String id;
        private String name;
        private String description;
        private String adventureStart;
        private String worldId;
        private String personaId;
        private String channelId;
        private String visibility;
        private String aiModel;
        private String moderation;
        private String gameMode;
        private String nudge;
        private String remember;
        private String authorsNote;
        private String bump;
        private int bumpFrequency;
        private boolean isMultiplayer;
        private int maxTokenLimit;
        private double temperature;
        private double frequencyPenalty;
        private double presencePenalty;
        private String ownerId;
        private OffsetDateTime creationDate;
        private OffsetDateTime lastUpdateDate;
        private Map<String, Double> logitBias;
        private Set<String> stopSequences;
        private Set<String> usersAllowedToRead;
        private Set<String> usersAllowedToWrite;

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

        public Builder worldId(String worldId) {
            this.worldId = worldId;
            return this;
        }

        public Builder personaId(String personaId) {
            this.personaId = personaId;
            return this;
        }

        public Builder channelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder visibility(String visibility) {
            this.visibility = visibility;
            return this;
        }

        public Builder aiModel(String aiModel) {
            this.aiModel = aiModel;
            return this;
        }

        public Builder moderation(String moderation) {
            this.moderation = moderation;
            return this;
        }

        public Builder gameMode(String gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        public Builder nudge(String nudge) {
            this.nudge = nudge;
            return this;
        }

        public Builder remember(String remember) {
            this.remember = remember;
            return this;
        }

        public Builder authorsNote(String authorsNote) {
            this.authorsNote = authorsNote;
            return this;
        }

        public Builder bump(String bump) {
            this.bump = bump;
            return this;
        }

        public Builder bumpFrequency(int bumpFrequency) {
            this.bumpFrequency = bumpFrequency;
            return this;
        }

        public Builder isMultiplayer(boolean isMultiplayer) {
            this.isMultiplayer = isMultiplayer;
            return this;
        }

        public Builder maxTokenLimit(int maxTokenLimit) {
            this.maxTokenLimit = maxTokenLimit;
            return this;
        }

        public Builder temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder frequencyPenalty(double frequencyPenalty) {
            this.frequencyPenalty = frequencyPenalty;
            return this;
        }

        public Builder presencePenalty(double presencePenalty) {
            this.presencePenalty = presencePenalty;
            return this;
        }

        public Builder stopSequences(Set<String> stopSequences) {

            this.stopSequences = stopSequences;
            return this;
        }

        public Builder logitBias(Map<String, Double> logitBias) {

            this.logitBias = logitBias;
            return this;
        }

        public Builder ownerId(String ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public Builder usersAllowedToRead(Set<String> usersAllowedToRead) {

            this.usersAllowedToRead = usersAllowedToRead;
            return this;
        }

        public Builder usersAllowedToWrite(Set<String> usersAllowedToWrite) {

            this.usersAllowedToWrite = usersAllowedToWrite;
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

        public GetAdventureResult build() {
            return new GetAdventureResult(this);
        }
    }
}