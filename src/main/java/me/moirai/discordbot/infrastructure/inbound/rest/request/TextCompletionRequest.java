package me.moirai.discordbot.infrastructure.inbound.rest.request;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class TextCompletionRequest {

    @NotNull(message = "cannot be null")
    @NotEmpty(message = "cannot be empty")
    private List<Message> messages;

    @NotNull(message = "cannot be null")
    private String personaId;

    @NotNull(message = "cannot be null")
    private String worldId;

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

    @NotEmpty(message = "cannot be empty")
    @NotNull(message = "cannot be null")
    private String aiModel;

    @NotNull(message = "cannot be null")
    private String moderationLevel;

    private Set<String> stopSequences;
    private Map<String, Double> logitBias;

    public static final class Message {

        @NotNull(message = "cannot be null")
        private Boolean isAuthorBot;

        @NotNull(message = "cannot be null")
        private String messageContent;

        public Message() {
        }

        public boolean getIsAuthorBot() {
            return isAuthorBot;
        }

        public void setIsAuthorBot(boolean isAuthorBot) {
            this.isAuthorBot = isAuthorBot;
        }

        public String getMessageContent() {
            return messageContent;
        }

        public void setMessageContent(String messageContent) {
            this.messageContent = messageContent;
        }
    }

    public TextCompletionRequest() {
    }

    public String getPersonaId() {
        return personaId;
    }

    public void setPersonaId(String personaId) {
        this.personaId = personaId;
    }

    public String getWorldId() {
        return worldId;
    }

    public void setWorldId(String worldId) {
        this.worldId = worldId;
    }

    public Integer getMaxTokenLimit() {
        return maxTokenLimit;
    }

    public void setMaxTokenLimit(Integer maxTokenLimit) {
        this.maxTokenLimit = maxTokenLimit;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getFrequencyPenalty() {
        return frequencyPenalty;
    }

    public void setFrequencyPenalty(Double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }

    public Double getPresencePenalty() {
        return presencePenalty;
    }

    public void setPresencePenalty(Double presencePenalty) {
        this.presencePenalty = presencePenalty;
    }

    public String getAiModel() {
        return aiModel;
    }

    public void setAiModel(String aiModel) {
        this.aiModel = aiModel;
    }

    public Set<String> getStopSequences() {
        return stopSequences;
    }

    public void setStopSequences(Set<String> stopSequences) {
        this.stopSequences = stopSequences;
    }

    public Map<String, Double> getLogitBias() {
        return logitBias;
    }

    public void setLogitBias(Map<String, Double> logitBias) {
        this.logitBias = logitBias;
    }

    public String getModerationLevel() {
        return moderationLevel;
    }

    public void setModerationLevel(String moderationLevel) {
        this.moderationLevel = moderationLevel;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
