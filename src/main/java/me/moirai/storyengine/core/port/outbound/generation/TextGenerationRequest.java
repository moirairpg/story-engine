package me.moirai.storyengine.core.port.outbound.generation;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.moirai.storyengine.common.dto.ChatMessage;

public final class TextGenerationRequest {

    private final String model;
    private final List<ChatMessage> messages;
    private final Set<String> stopSequences;
    private final Integer maxTokens;
    private final Double temperature;
    private final Double presencePenalty;
    private final Double frequencyPenalty;
    private final Map<String, Double> logitBias;

    public TextGenerationRequest(Builder builder) {

        this.model = builder.model;
        this.maxTokens = builder.maxTokens;
        this.temperature = builder.temperature;
        this.presencePenalty = builder.presencePenalty;
        this.frequencyPenalty = builder.frequencyPenalty;
        this.messages = unmodifiableList(builder.messages);
        this.stopSequences = unmodifiableSet(builder.stopSequences);
        this.logitBias = unmodifiableMap(builder.logitBias);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getModel() {
        return model;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public Set<String> getStopSequences() {
        return stopSequences;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double getPresencePenalty() {
        return presencePenalty;
    }

    public Double getFrequencyPenalty() {
        return frequencyPenalty;
    }

    public Map<String, Double> getLogitBias() {
        return logitBias;
    }

    public static final class Builder {

        private String model;
        private List<ChatMessage> messages = new ArrayList<>();
        private Set<String> stopSequences = new HashSet<>();
        private Integer maxTokens;
        private Double temperature;
        private Double presencePenalty;
        private Double frequencyPenalty;
        private Map<String, Double> logitBias = new HashMap<>();

        private Builder() {
        }

        public Builder model(String model) {

            this.model = model;
            return this;
        }

        public Builder messages(List<ChatMessage> messages) {

            if (messages != null) {
                this.messages = messages;
            }

            return this;
        }

        public Builder stopSequences(Set<String> stopSequences) {

            if (stopSequences != null) {
                this.stopSequences = stopSequences;
            }

            return this;
        }

        public Builder maxTokens(Integer maxTokens) {

            this.maxTokens = maxTokens;
            return this;
        }

        public Builder temperature(Double temperature) {

            this.temperature = temperature;
            return this;
        }

        public Builder presencePenalty(Double presencePenalty) {

            this.presencePenalty = presencePenalty;
            return this;
        }

        public Builder frequencyPenalty(Double frequencyPenalty) {

            this.frequencyPenalty = frequencyPenalty;
            return this;
        }

        public Builder logitBias(Map<String, Double> logitBias) {

            if (logitBias != null) {
                this.logitBias = logitBias;
            }

            return this;
        }

        public TextGenerationRequest build() {

            return new TextGenerationRequest(this);
        }
    }
}
