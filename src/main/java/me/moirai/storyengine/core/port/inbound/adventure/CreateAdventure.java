package me.moirai.storyengine.core.port.inbound.adventure;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.MapUtils;

import me.moirai.storyengine.common.usecases.UseCase;

public final class CreateAdventure extends UseCase<AdventureDetails> {

    private final String name;
    private final String description;
    private final String worldId;
    private final String personaId;
    private final String channelId;
    private final String visibility;
    private final String aiModel;
    private final String moderation;
    private final String requesterId;
    private final String gameMode;
    private final String nudge;
    private final String remember;
    private final String authorsNote;
    private final String bump;
    private final Integer bumpFrequency;
    private final Integer maxTokenLimit;
    private final Double temperature;
    private final Double frequencyPenalty;
    private final Double presencePenalty;
    private final Map<String, Double> logitBias;
    private final Set<String> stopSequences;
    private final Set<String> usersAllowedToWrite;
    private final Set<String> usersAllowedToRead;
    private final boolean isMultiplayer;

    private CreateAdventure(Builder builder) {

        this.name = builder.name;
        this.description = builder.description;
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
        this.requesterId = builder.requesterId;
        this.gameMode = builder.gameMode;
        this.isMultiplayer = builder.isMultiplayer;
        this.nudge = builder.nudge;
        this.remember = builder.remember;
        this.authorsNote = builder.authorsNote;
        this.bump = builder.bump;
        this.bumpFrequency = builder.bumpFrequency;

        this.logitBias = MapUtils.isEmpty(builder.logitBias) ? Collections.emptyMap()
                : unmodifiableMap(builder.logitBias);

        this.stopSequences = isEmpty(builder.stopSequences) ? Collections.emptySet()
                : unmodifiableSet(builder.stopSequences);

        this.usersAllowedToWrite = isEmpty(builder.usersAllowedToWrite) ? Collections.emptySet()
                : unmodifiableSet(builder.usersAllowedToWrite);

        this.usersAllowedToRead = isEmpty(builder.usersAllowedToRead) ? Collections.emptySet()
                : unmodifiableSet(builder.usersAllowedToRead);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
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

    public Integer getBumpFrequency() {
        return bumpFrequency;
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

    public Set<String> getUsersAllowedToWrite() {
        return usersAllowedToWrite;
    }

    public Set<String> getUsersAllowedToRead() {
        return usersAllowedToRead;
    }

    public String getRequesterDiscordId() {
        return requesterId;
    }

    public static final class Builder {

        private String name;
        private String description;
        private String worldId;
        private String personaId;
        private String channelId;
        private String visibility;
        private String aiModel;
        private String moderation;
        private String requesterId;
        private String gameMode;
        private String nudge;
        private String remember;
        private String authorsNote;
        private String bump;
        private Integer bumpFrequency;
        private Integer maxTokenLimit;
        private Double temperature;
        private Double frequencyPenalty;
        private Double presencePenalty;
        private Map<String, Double> logitBias;
        private Set<String> stopSequences;
        private Set<String> usersAllowedToWrite;
        private Set<String> usersAllowedToRead;
        private boolean isMultiplayer;

        private Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
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

        public Builder gameMode(String gameMode) {
            this.gameMode = gameMode;
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

            this.stopSequences = stopSequences;
            return this;
        }

        public Builder logitBias(Map<String, Double> logitBias) {

            this.logitBias = logitBias;
            return this;
        }

        public Builder usersAllowedToWrite(Set<String> usersAllowedToWrite) {

            this.usersAllowedToWrite = usersAllowedToWrite;
            return this;
        }

        public Builder usersAllowedToRead(Set<String> usersAllowedToRead) {

            this.usersAllowedToRead = usersAllowedToRead;
            return this;
        }

        public Builder requesterId(String requesterId) {
            this.requesterId = requesterId;
            return this;
        }

        public CreateAdventure build() {
            return new CreateAdventure(this);
        }
    }
}