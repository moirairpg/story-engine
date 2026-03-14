package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import me.moirai.storyengine.common.dto.ChatMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompletionRequest {

    @JsonProperty("model")
    private String model;

    @JsonProperty("messages")
    private List<ChatMessage> messages;

    @JsonProperty("stop")
    private Set<String> stop;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    @JsonProperty("temperature")
    private Double temperature;

    @JsonProperty("presence_penalty")
    private Double presencePenalty;

    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty;

    @JsonProperty("logit_bias")
    private Map<String, Double> logitBias;

    public CompletionRequest() {
    }

    private CompletionRequest(Builder builder) {

        this.model = builder.model;
        this.messages = builder.messages;
        this.stop = builder.stop;
        this.maxTokens = builder.maxTokens;
        this.temperature = builder.temperature;
        this.presencePenalty = builder.presencePenalty;
        this.frequencyPenalty = builder.frequencyPenalty;
        this.logitBias = builder.logitBias;
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

    public Set<String> getStop() {
        return stop;
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

    public void setModel(String model) {
        this.model = model;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public void setStop(Set<String> stop) {
        this.stop = stop;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public void setPresencePenalty(Double presencePenalty) {
        this.presencePenalty = presencePenalty;
    }

    public void setFrequencyPenalty(Double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }

    public void setLogitBias(Map<String, Double> logitBias) {
        this.logitBias = logitBias;
    }

    public static final class Builder {

        private String model;
        private List<ChatMessage> messages;
        private Set<String> stop;
        private Integer maxTokens;
        private Double temperature;
        private Double presencePenalty;
        private Double frequencyPenalty;
        private Map<String, Double> logitBias;

        private Builder() {
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder messages(List<ChatMessage> messages) {
            this.messages = messages;
            return this;
        }

        public Builder stop(Set<String> stop) {
            this.stop = stop;
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
            this.logitBias = logitBias;
            return this;
        }

        public CompletionRequest build() {
            return new CompletionRequest(this);
        }
    }
}