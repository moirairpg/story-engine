package me.moirai.storyengine.core.port.inbound.adventure;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.MapUtils.isEmpty;

import java.util.Map;
import java.util.Set;

import me.moirai.storyengine.common.usecases.UseCase;

public final class UpdateAdventure extends UseCase<UpdateAdventureResult> {

    private final String id;
    private final String description;
    private final String adventureStart;
    private final String name;
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
    private final Map<String, Double> logitBiasToAdd;
    private final Set<String> stopSequencesToAdd;
    private final Set<String> stopSequencesToRemove;
    private final Set<String> logitBiasToRemove;
    private final Set<String> usersAllowedToWriteToAdd;
    private final Set<String> usersAllowedToWriteToRemove;
    private final Set<String> usersAllowedToReadToAdd;
    private final Set<String> usersAllowedToReadToRemove;
    private final boolean isMultiplayer;

    private UpdateAdventure(Builder builder) {

        this.id = builder.id;
        this.name = builder.name;
        this.adventureStart = builder.adventureStart;
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

        this.logitBiasToAdd = isEmpty(builder.logitBiasToAdd) ? emptyMap()
                : unmodifiableMap(builder.logitBiasToAdd);

        this.logitBiasToRemove = isEmpty(builder.logitBiasToRemove) ? emptySet()
                : unmodifiableSet(builder.logitBiasToRemove);

        this.stopSequencesToAdd = isEmpty(builder.stopSequencesToAdd) ? emptySet()
                : unmodifiableSet(builder.stopSequencesToAdd);

        this.stopSequencesToRemove = isEmpty(builder.stopSequencesToRemove) ? emptySet()
                : unmodifiableSet(builder.stopSequencesToRemove);

        this.usersAllowedToWriteToAdd = isEmpty(builder.usersAllowedToWriteToAdd) ? emptySet()
                : unmodifiableSet(builder.usersAllowedToWriteToAdd);

        this.usersAllowedToWriteToRemove = isEmpty(builder.usersAllowedToWriteToRemove) ? emptySet()
                : unmodifiableSet(builder.usersAllowedToWriteToRemove);

        this.usersAllowedToReadToAdd = isEmpty(builder.usersAllowedToReadToAdd) ? emptySet()
                : unmodifiableSet(builder.usersAllowedToReadToAdd);

        this.usersAllowedToReadToRemove = isEmpty(builder.usersAllowedToReadToRemove) ? emptySet()
                : unmodifiableSet(builder.usersAllowedToReadToRemove);
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

    public String getAdventureStart() {
        return adventureStart;
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

    public Set<String> getStopSequencesToAdd() {
        return stopSequencesToAdd;
    }

    public Set<String> getStopSequencesToRemove() {
        return stopSequencesToRemove;
    }

    public Map<String, Double> getLogitBiasToAdd() {
        return logitBiasToAdd;
    }

    public Set<String> getLogitBiasToRemove() {
        return logitBiasToRemove;
    }

    public Set<String> getUsersAllowedToWriteToAdd() {
        return usersAllowedToWriteToAdd;
    }

    public Set<String> getUsersAllowedToWriteToRemove() {
        return usersAllowedToWriteToRemove;
    }

    public Set<String> getUsersAllowedToReadToAdd() {
        return usersAllowedToReadToAdd;
    }

    public Set<String> getUsersAllowedToReadToRemove() {
        return usersAllowedToReadToRemove;
    }

    public String getRequesterDiscordId() {
        return requesterId;
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

    public String getDescription() {
        return description;
    }

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
        private String requesterId;
        private String nudge;
        private String remember;
        private String authorsNote;
        private String bump;
        private Integer bumpFrequency;
        private Double temperature;
        private Double frequencyPenalty;
        private Double presencePenalty;
        private Integer maxTokenLimit;
        private Map<String, Double> logitBiasToAdd;
        private Set<String> logitBiasToRemove;
        private Set<String> stopSequencesToAdd;
        private Set<String> stopSequencesToRemove;
        private Set<String> usersAllowedToWriteToAdd;
        private Set<String> usersAllowedToWriteToRemove;
        private Set<String> usersAllowedToReadToAdd;
        private Set<String> usersAllowedToReadToRemove;
        private boolean isMultiplayer;

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

        public Builder stopSequencesToAdd(Set<String> stopSequencesToAdd) {

            this.stopSequencesToAdd = stopSequencesToAdd;
            return this;
        }

        public Builder stopSequencesToRemove(Set<String> stopSequencesToRemove) {

            this.stopSequencesToRemove = stopSequencesToRemove;
            return this;
        }

        public Builder logitBiasToAdd(Map<String, Double> logitBiasToAdd) {

            this.logitBiasToAdd = logitBiasToAdd;
            return this;
        }

        public Builder logitBiasToRemove(Set<String> logitBiasToRemove) {

            this.logitBiasToRemove = logitBiasToRemove;
            return this;
        }

        public Builder usersAllowedToWriteToAdd(Set<String> usersAllowedToWriteToAdd) {

            this.usersAllowedToWriteToAdd = usersAllowedToWriteToAdd;
            return this;
        }

        public Builder usersAllowedToWriteToRemove(Set<String> usersAllowedToWriteToRemove) {

            this.usersAllowedToWriteToRemove = usersAllowedToWriteToRemove;
            return this;
        }

        public Builder usersAllowedToReadToAdd(Set<String> usersAllowedToReadToAdd) {

            this.usersAllowedToReadToAdd = usersAllowedToReadToAdd;
            return this;
        }

        public Builder usersAllowedToReadToRemove(Set<String> usersAllowedToReadToRemove) {

            this.usersAllowedToReadToRemove = usersAllowedToReadToRemove;
            return this;
        }

        public Builder requesterId(String requesterId) {
            this.requesterId = requesterId;
            return this;
        }

        public UpdateAdventure build() {
            return new UpdateAdventure(this);
        }
    }
}