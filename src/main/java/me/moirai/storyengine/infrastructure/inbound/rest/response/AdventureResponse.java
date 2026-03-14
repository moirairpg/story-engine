package me.moirai.storyengine.infrastructure.inbound.rest.response;

import static org.apache.commons.collections4.MapUtils.emptyIfNull;
import static org.apache.commons.collections4.SetUtils.emptyIfNull;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdventureResponse {

    private String id;
    private String name;
    private String worldId;
    private String personaId;
    private String channelId;
    private String visibility;
    private String aiModel;
    private String moderation;
    private String gameMode;
    private boolean isMultiplayer;
    private Integer maxTokenLimit;
    private Double temperature;
    private Double frequencyPenalty;
    private Double presencePenalty;
    private Set<String> stopSequences;
    private Map<String, Double> logitBias;
    private String ownerId;
    private Set<String> usersAllowedToRead;
    private Set<String> usersAllowedToWrite;
    private OffsetDateTime creationDate;
    private OffsetDateTime lastUpdateDate;
    private String nudge;
    private String authorsNote;
    private String remember;
    private String bump;
    private Integer bumpFrequency;

    public AdventureResponse() {
    }

    private AdventureResponse(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.worldId = builder.worldId;
        this.personaId = builder.personaId;
        this.channelId = builder.channelId;
        this.visibility = builder.visibility;
        this.aiModel = builder.aiModel;
        this.moderation = builder.moderation;
        this.maxTokenLimit = builder.maxTokenLimit;
        this.temperature = builder.temperature;
        this.frequencyPenalty = builder.frequencyPenalty;
        this.presencePenalty = builder.presencePenalty;
        this.stopSequences = builder.stopSequences;
        this.logitBias = builder.logitBias;
        this.ownerId = builder.ownerId;
        this.usersAllowedToRead = builder.usersAllowedToRead;
        this.usersAllowedToWrite = builder.usersAllowedToWrite;
        this.creationDate = builder.creationDate;
        this.lastUpdateDate = builder.lastUpdateDate;
        this.gameMode = builder.gameMode;
        this.isMultiplayer = builder.isMultiplayer;
        this.nudge = builder.nudge;
        this.authorsNote = builder.authorsNote;
        this.remember = builder.remember;
        this.bump = builder.bump;
        this.bumpFrequency = builder.bumpFrequency;
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

    public Integer getMaxTokenLimit() {
        return maxTokenLimit;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double getFrequencyPenalty() {
        return frequencyPenalty;
    }

    public Double getPresencePenalty() {
        return presencePenalty;
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

    public String getAuthorsNote() {
        return authorsNote;
    }

    public String getRemember() {
        return remember;
    }

    public String getBump() {
        return bump;
    }

    public Integer getBumpFrequency() {
        return bumpFrequency;
    }

    public static final class Builder {

        private String id;
        private String name;
        private String worldId;
        private String personaId;
        private String channelId;
        private String visibility;
        private String aiModel;
        private String moderation;
        private String gameMode;
        private boolean isMultiplayer;
        private Integer maxTokenLimit;
        private Double temperature;
        private Double frequencyPenalty;
        private Double presencePenalty;
        private Set<String> stopSequences;
        private Map<String, Double> logitBias;
        private String ownerId;
        private Set<String> usersAllowedToRead;
        private Set<String> usersAllowedToWrite;
        private OffsetDateTime creationDate;
        private OffsetDateTime lastUpdateDate;
        private String nudge;
        private String authorsNote;
        private String remember;
        private String bump;
        private Integer bumpFrequency;

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

        public Builder authorsNote(String authorsNote) {
            this.authorsNote = authorsNote;
            return this;
        }

        public Builder remember(String remember) {
            this.remember = remember;
            return this;
        }

        public Builder bump(String bump) {
            this.bump = bump;
            return this;
        }

        public Builder bumpFrequency(Integer bumpFrequency) {
            this.bumpFrequency = bumpFrequency;
            return this;
        }

        public Builder isMultiplayer(boolean isMultiplayer) {
            this.isMultiplayer = isMultiplayer;
            return this;
        }

        public Builder maxTokenLimit(Integer maxTokenLimit) {
            this.maxTokenLimit = maxTokenLimit;
            return this;
        }

        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder frequencyPenalty(Double frequencyPenalty) {
            this.frequencyPenalty = frequencyPenalty;
            return this;
        }

        public Builder presencePenalty(Double presencePenalty) {
            this.presencePenalty = presencePenalty;
            return this;
        }

        public Builder stopSequences(Set<String> stopSequences) {
            this.stopSequences = emptyIfNull(stopSequences);
            return this;
        }

        public Builder logitBias(Map<String, Double> logitBias) {
            this.logitBias = emptyIfNull(logitBias);
            return this;
        }

        public Builder ownerId(String ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public Builder usersAllowedToRead(Set<String> usersAllowedToRead) {
            this.usersAllowedToRead = emptyIfNull(usersAllowedToRead);
            return this;
        }

        public Builder usersAllowedToWrite(Set<String> usersAllowedToWrite) {
            this.usersAllowedToWrite = emptyIfNull(usersAllowedToWrite);
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

        public AdventureResponse build() {
            return new AdventureResponse(this);
        }
    }
}