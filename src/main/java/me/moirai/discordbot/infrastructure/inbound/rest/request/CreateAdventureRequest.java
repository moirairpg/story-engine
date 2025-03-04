package me.moirai.discordbot.infrastructure.inbound.rest.request;

import java.util.Map;
import java.util.Set;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class CreateAdventureRequest {

    @NotEmpty(message = "cannot be empty")
    @NotNull(message = "cannot be null")
    private String name;

    @NotEmpty(message = "cannot be empty")
    @NotNull(message = "cannot be null")
    private String worldId;

    @NotEmpty(message = "cannot be empty")
    @NotNull(message = "cannot be null")
    private String personaId;

    @NotEmpty(message = "cannot be empty")
    @NotNull(message = "cannot be null")
    private String discordChannelId;

    @NotEmpty(message = "cannot be empty")
    @NotNull(message = "cannot be null")
    private String visibility;

    @NotEmpty(message = "cannot be empty")
    @NotNull(message = "cannot be null")
    private String aiModel;

    @NotEmpty(message = "cannot be empty")
    @NotNull(message = "cannot be null")
    private String moderation;

    @NotNull(message = "cannot be null")
    @Min(value = 100, message = "cannot be less than 100")
    private Integer maxTokenLimit;

    @NotNull(message = "cannot be null")
    @DecimalMin(value = "0.1", message = "cannot be less than 0.1")
    @DecimalMax(value = "2", message = "cannot be greater than 2")
    private Double temperature;

    @DecimalMin(value = "-2", message = "cannot be less than -2")
    @DecimalMax(value = "2", message = "cannot be greater than 2")
    private Double frequencyPenalty;

    @DecimalMin(value = "-2", message = "cannot be less than -2")
    @DecimalMax(value = "2", message = "cannot be greater than 2")
    private Double presencePenalty;

    private String gameMode;
    private boolean isMultiplayer;
    private Set<String> stopSequences;
    private Map<String, Double> logitBias;
    private Set<String> usersAllowedToWrite;
    private Set<String> usersAllowedToRead;
    private String nudge;
    private String authorsNote;
    private String remember;
    private String bump;
    private Integer bumpFrequency;

    public CreateAdventureRequest() {
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

    public String getDiscordChannelId() {
        return discordChannelId;
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

    public Set<String> getUsersAllowedToWrite() {
        return usersAllowedToWrite;
    }

    public Set<String> getUsersAllowedToRead() {
        return usersAllowedToRead;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public void setMultiplayer(boolean isMultiplayer) {
        this.isMultiplayer = isMultiplayer;
    }

    public void setWorldId(String worldId) {
        this.worldId = worldId;
    }

    public void setPersonaId(String personaId) {
        this.personaId = personaId;
    }

    public void setDiscordChannelId(String discordChannelId) {
        this.discordChannelId = discordChannelId;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public void setAiModel(String aiModel) {
        this.aiModel = aiModel;
    }

    public void setModeration(String moderation) {
        this.moderation = moderation;
    }

    public void setMaxTokenLimit(Integer maxTokenLimit) {
        this.maxTokenLimit = maxTokenLimit;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public void setFrequencyPenalty(Double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }

    public void setPresencePenalty(Double presencePenalty) {
        this.presencePenalty = presencePenalty;
    }

    public void setStopSequences(Set<String> stopSequences) {
        this.stopSequences = stopSequences;
    }

    public void setLogitBias(Map<String, Double> logitBias) {
        this.logitBias = logitBias;
    }

    public void setUsersAllowedToWrite(Set<String> usersAllowedToWrite) {
        this.usersAllowedToWrite = usersAllowedToWrite;
    }

    public void setUsersAllowedToRead(Set<String> usersAllowedToRead) {
        this.usersAllowedToRead = usersAllowedToRead;
    }

    public String getNudge() {
        return nudge;
    }

    public void setNudge(String nudge) {
        this.nudge = nudge;
    }

    public String getAuthorsNote() {
        return authorsNote;
    }

    public void setAuthorsNote(String authorsNote) {
        this.authorsNote = authorsNote;
    }

    public String getRemember() {
        return remember;
    }

    public void setRemember(String remember) {
        this.remember = remember;
    }

    public String getBump() {
        return bump;
    }

    public void setBump(String bump) {
        this.bump = bump;
    }

    public Integer getBumpFrequency() {
        return bumpFrequency;
    }

    public void setBumpFrequency(Integer bumpFrequency) {
        this.bumpFrequency = bumpFrequency;
    }
}
