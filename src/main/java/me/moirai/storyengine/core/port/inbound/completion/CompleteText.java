package me.moirai.storyengine.core.port.inbound.completion;

import java.util.List;
import java.util.Map;
import java.util.Set;

import me.moirai.storyengine.common.usecases.UseCase;
import reactor.core.publisher.Mono;

public class CompleteText extends UseCase<Mono<CompleteTextResult>> {

    private final String personaId;
    private final String worldId;
    private final Integer maxTokenLimit;
    private final Double temperature;
    private final Double frequencyPenalty;
    private final Double presencePenalty;
    private final String aiModel;
    private final Set<String> stopSequences;
    private final Map<String, Double> logitBias;
    private final String requesterId;
    private final String moderationLevel;
    private final List<Message> messages;

    public static final class Message {

        private final boolean isAuthorBot;
        private final String messageContent;

        private Message(Builder builder) {
            this.isAuthorBot = builder.isAuthorBot;
            this.messageContent = builder.messageContent;
        }

        public static Builder builder() {
            return new Builder();
        }

        public boolean isAuthorBot() {
            return isAuthorBot;
        }

        public String getMessageContent() {
            return messageContent;
        }

        public static final class Builder {

            private boolean isAuthorBot;
            private String messageContent;

            public Builder isAuthorBot(boolean isAuthorBot) {
                this.isAuthorBot = isAuthorBot;
                return this;
            }

            public Builder messageContent(String messageContent) {
                this.messageContent = messageContent;
                return this;
            }

            public Message build() {
                return new Message(this);
            }
        }
    }

    public CompleteText(Builder builder) {

        this.messages = builder.messages;
        this.personaId = builder.personaId;
        this.worldId = builder.worldId;
        this.maxTokenLimit = builder.maxTokenLimit;
        this.temperature = builder.temperature;
        this.frequencyPenalty = builder.frequencyPenalty;
        this.presencePenalty = builder.presencePenalty;
        this.aiModel = builder.aiModel;
        this.stopSequences = builder.stopSequences;
        this.logitBias = builder.logitBias;
        this.requesterId = builder.requesterId;
        this.moderationLevel = builder.moderationLevel;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getPersonaId() {
        return personaId;
    }

    public String getWorldId() {
        return worldId;
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

    public String getAiModel() {
        return aiModel;
    }

    public Set<String> getStopSequences() {
        return stopSequences;
    }

    public Map<String, Double> getLogitBias() {
        return logitBias;
    }

    public String getRequesterDiscordId() {
        return requesterId;
    }

    public String getModerationLevel() {
        return moderationLevel;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public static final class Builder {

        private String personaId;
        private String worldId;
        private Integer maxTokenLimit;
        private Double temperature;
        private Double frequencyPenalty;
        private Double presencePenalty;
        private String aiModel;
        private Set<String> stopSequences;
        private Map<String, Double> logitBias;
        private String requesterId;
        private String moderationLevel;
        private List<Message> messages;

        private Builder() {
        }

        public Builder messages(List<Message> messages) {
            this.messages = messages;
            return this;
        }

        public Builder personaId(String personaId) {
            this.personaId = personaId;
            return this;
        }

        public Builder worldId(String worldId) {
            this.worldId = worldId;
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

        public Builder aiModel(String aiModel) {
            this.aiModel = aiModel;
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

        public Builder requesterId(String requesterId) {
            this.requesterId = requesterId;
            return this;
        }

        public Builder moderationLevel(String moderationLevel) {
            this.moderationLevel = moderationLevel;
            return this;
        }

        public CompleteText build() {
            return new CompleteText(this);
        }
    }
}
