package me.moirai.storyengine.infrastructure.outbound.adapter.request;

import static java.util.Collections.emptySet;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Collections.unmodifiableMap;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;

public class ModelConfigurationRequest {

    private final AiModelRequest aiModel;
    private final Integer maxTokenLimit;
    private final Double temperature;
    private final Double frequencyPenalty;
    private final Double presencePenalty;
    private final Set<String> stopSequences;
    private final Map<String, Double> logitBias;

    private ModelConfigurationRequest(Builder builder) {

        this.aiModel = builder.aiModel;
        this.maxTokenLimit = builder.maxTokenLimit;
        this.temperature = builder.temperature;
        this.frequencyPenalty = builder.frequencyPenalty;
        this.presencePenalty = builder.presencePenalty;

        this.stopSequences = unmodifiableSet(
                builder.stopSequences == null ? emptySet() : new HashSet<>(builder.stopSequences));

        this.logitBias = unmodifiableMap(
                builder.logitBias == null ? emptyMap() : new HashMap<>(builder.logitBias));
    }

    public static Builder builder() {

        return new Builder();
    }

    public AiModelRequest getAiModel() {
        return aiModel;
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

    public static final class Builder {

        private AiModelRequest aiModel;
        private Integer maxTokenLimit;
        private Double temperature;
        private Double frequencyPenalty;
        private Double presencePenalty;
        private Set<String> stopSequences = new HashSet<>();
        private Map<String, Double> logitBias = new HashMap<>();

        private Builder() {
        }

        public Builder aiModel(AiModelRequest aiModel) {

            this.aiModel = aiModel;
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

            if (stopSequences != null) {
                this.stopSequences = stopSequences;
            }

            return this;
        }

        public Builder logitBias(Map<String, Double> logitBias) {

            if (logitBias != null) {
                this.logitBias = logitBias;
            }

            return this;
        }

        public ModelConfigurationRequest build() {

            return new ModelConfigurationRequest(this);
        }
    }
}
