package me.moirai.storyengine.core.domain.adventure;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.apache.commons.collections4.MapUtils.emptyIfNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import me.moirai.storyengine.common.dbutil.StringMapDoubleConverter;
import me.moirai.storyengine.common.dbutil.StringSetConverter;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

@Embeddable
public final class ModelConfiguration {

    private static final double DEFAULT_FREQUENCY_PENALTY = 0.0;
    private static final double DEFAULT_PRESENCE_PENALTY = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_model", nullable = false)
    private ArtificialIntelligenceModel aiModel;

    @Column(name = "max_token_limit", nullable = false)
    private int maxTokenLimit;

    @Column(name = "temperature", nullable = false)
    private Double temperature;

    @Column(name = "frequency_penalty", nullable = false)
    private Double frequencyPenalty;

    @Column(name = "presence_penalty", nullable = false)
    private Double presencePenalty;

    @Column(name = "stop_sequences")
    @Convert(converter = StringSetConverter.class)
    private Set<String> stopSequences;

    @Column(name = "logit_bias")
    @Convert(converter = StringMapDoubleConverter.class)
    private Map<String, Double> logitBias;

    private ModelConfiguration(Builder builder) {

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

    protected ModelConfiguration() {
        super();
    }

    public static Builder builder() {

        return new Builder();
    }

    private Builder cloneFrom(ModelConfiguration modelConfiguration) {

        return builder()
                .aiModel(modelConfiguration.getAiModel())
                .maxTokenLimit(modelConfiguration.getMaxTokenLimit())
                .temperature(modelConfiguration.getTemperature())
                .frequencyPenalty(modelConfiguration.getFrequencyPenalty())
                .presencePenalty(modelConfiguration.getPresencePenalty())
                .stopSequences(modelConfiguration.getStopSequences())
                .logitBias(modelConfiguration.getLogitBias());
    }

    public ArtificialIntelligenceModel getAiModel() {
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

    public ModelConfiguration updateAiModel(ArtificialIntelligenceModel aiModel) {

        return cloneFrom(this).aiModel(aiModel).build();
    }

    public ModelConfiguration updateMaxTokenLimit(Integer maxTokenLimit) {

        validateMaxTokenLimit(maxTokenLimit, aiModel);

        return cloneFrom(this).maxTokenLimit(maxTokenLimit).build();
    }

    public ModelConfiguration updateTemperature(Double temperature) {

        validateTemperature(temperature);

        return cloneFrom(this).temperature(temperature).build();
    }

    public ModelConfiguration updateFrequencyPenalty(Double frequencyPenalty) {

        if (frequencyPenalty == null) {
            frequencyPenalty = DEFAULT_FREQUENCY_PENALTY;
        }

        validateFrequencyPenalty(frequencyPenalty);

        return cloneFrom(this).frequencyPenalty(frequencyPenalty).build();
    }

    public ModelConfiguration updatePresencePenalty(Double presencePenalty) {

        if (presencePenalty == null) {
            presencePenalty = DEFAULT_PRESENCE_PENALTY;
        }

        validatePresencePenalty(presencePenalty);

        return cloneFrom(this).presencePenalty(presencePenalty).build();
    }

    public ModelConfiguration addStopSequence(String stopSequence) {

        Set<String> newStopSequences = new HashSet<>(this.stopSequences);
        newStopSequences.add(stopSequence);

        return cloneFrom(this).stopSequences(newStopSequences).build();
    }

    public ModelConfiguration removeStopSequence(String stopSequence) {

        Set<String> newStopSequences = new HashSet<>(this.stopSequences);
        newStopSequences.remove(stopSequence);

        return cloneFrom(this).stopSequences(newStopSequences).build();
    }

    public ModelConfiguration addLogitBias(String token, Double bias) {

        validateLogitBias(bias);

        Map<String, Double> newLogitBias = new HashMap<>(this.logitBias);
        newLogitBias.put(token, bias);

        return cloneFrom(this).logitBias(newLogitBias).build();
    }

    public ModelConfiguration removeLogitBias(String token) {

        Map<String, Double> newLogitBias = new HashMap<>(this.logitBias);
        newLogitBias.remove(token);

        return cloneFrom(this).logitBias(newLogitBias).build();
    }

    private static void validateTemperature(double temperature) {

        if (temperature < 0.1 || temperature > 2) {
            throw new BusinessRuleViolationException("Temperature value has to be between 0 and 2");
        }
    }

    private static void validateFrequencyPenalty(Double frequencyPenalty) {

        if (frequencyPenalty < -2 || frequencyPenalty > 2) {
            throw new BusinessRuleViolationException("Frequency penalty needs to be between -2 and 2");
        }
    }

    private static void validatePresencePenalty(Double presencePenalty) {

        if (presencePenalty < -2 || presencePenalty > 2) {
            throw new BusinessRuleViolationException("Presence penalty needs to be between -2 and 2");
        }
    }

    private static void validateMaxTokenLimit(int maxTokenLimit, ArtificialIntelligenceModel aiModel) {

        if (maxTokenLimit < 100 || maxTokenLimit > aiModel.getHardTokenLimit()) {
            throw new BusinessRuleViolationException(
                    String.format("Max token limit has to be between 100 and %s", aiModel.getHardTokenLimit()));
        }
    }

    private static void validateLogitBias(double bias) {

        if (bias < -100 || bias > 100) {
            throw new BusinessRuleViolationException("Logit bias value needs to be between -100 and 100");
        }
    }

    public static final class Builder {

        private ArtificialIntelligenceModel aiModel;
        private Integer maxTokenLimit;
        private Double temperature;
        private Double frequencyPenalty;
        private Double presencePenalty;
        private Set<String> stopSequences;
        private Map<String, Double> logitBias;

        private Builder() {
        }

        public Builder aiModel(ArtificialIntelligenceModel aiModel) {

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

            this.stopSequences = stopSequences;
            return this;
        }

        public Builder logitBias(Map<String, Double> logitBias) {

            this.logitBias = logitBias;
            return this;
        }

        public ModelConfiguration build() {

            if (frequencyPenalty == null) {
                frequencyPenalty = DEFAULT_FREQUENCY_PENALTY;
            }

            if (presencePenalty == null) {
                presencePenalty = DEFAULT_PRESENCE_PENALTY;
            }

            validateTemperature(temperature);
            validateFrequencyPenalty(frequencyPenalty);
            validatePresencePenalty(presencePenalty);
            validateMaxTokenLimit(maxTokenLimit, aiModel);

            emptyIfNull(logitBias).entrySet()
                    .stream()
                    .forEach(entry -> validateLogitBias(entry.getValue()));

            return new ModelConfiguration(this);
        }
    }
}
