package me.moirai.storyengine.core.domain.adventure;

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
import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

@Embeddable
public record ModelConfiguration(
        @Enumerated(EnumType.STRING) @Column(name = "ai_model") ArtificialIntelligenceModel aiModel,
        @Column(name = "max_token_limit") int maxTokenLimit,
        @Column(name = "temperature") Double temperature,
        @Column(name = "frequency_penalty") Double frequencyPenalty,
        @Column(name = "presence_penalty") Double presencePenalty,
        @Column(name = "stop_sequences") @Convert(converter = StringSetConverter.class) Set<String> stopSequences,
        @Column(name = "logit_bias") @Convert(converter = StringMapDoubleConverter.class) Map<String, Double> logitBias) {

    private static final double DEFAULT_FREQUENCY_PENALTY = 0.0;
    private static final double DEFAULT_PRESENCE_PENALTY = 0.0;

    public ModelConfiguration {

        if (frequencyPenalty == null) {
            frequencyPenalty = DEFAULT_FREQUENCY_PENALTY;
        }

        if (presencePenalty == null) {
            presencePenalty = DEFAULT_PRESENCE_PENALTY;
        }

        if (stopSequences == null) {
            stopSequences = Set.of();
        }

        if (logitBias == null) {
            logitBias = Map.of();
        }

        validateTemperature(temperature);
        validateFrequencyPenalty(frequencyPenalty);
        validatePresencePenalty(presencePenalty);
        validateMaxTokenLimit(maxTokenLimit, aiModel);
        emptyIfNull(logitBias).values().forEach(ModelConfiguration::validateLogitBias);
    }

    public ModelConfiguration updateAiModel(ArtificialIntelligenceModel aiModel) {

        return new ModelConfiguration(aiModel, maxTokenLimit, temperature, frequencyPenalty, presencePenalty, stopSequences, logitBias);
    }

    public ModelConfiguration updateMaxTokenLimit(Integer maxTokenLimit) {

        return new ModelConfiguration(aiModel, maxTokenLimit, temperature, frequencyPenalty, presencePenalty, stopSequences, logitBias);
    }

    public ModelConfiguration updateTemperature(Double temperature) {

        return new ModelConfiguration(aiModel, maxTokenLimit, temperature, frequencyPenalty, presencePenalty, stopSequences, logitBias);
    }

    public ModelConfiguration updateFrequencyPenalty(Double frequencyPenalty) {

        return new ModelConfiguration(aiModel, maxTokenLimit, temperature, frequencyPenalty, presencePenalty, stopSequences, logitBias);
    }

    public ModelConfiguration updatePresencePenalty(Double presencePenalty) {

        return new ModelConfiguration(aiModel, maxTokenLimit, temperature, frequencyPenalty, presencePenalty, stopSequences, logitBias);
    }

    public ModelConfiguration addStopSequence(String stopSequence) {

        Set<String> newStopSequences = new HashSet<>(this.stopSequences);
        newStopSequences.add(stopSequence);
        return new ModelConfiguration(aiModel, maxTokenLimit, temperature, frequencyPenalty, presencePenalty, newStopSequences, logitBias);
    }

    public ModelConfiguration removeStopSequence(String stopSequence) {

        Set<String> newStopSequences = new HashSet<>(this.stopSequences);
        newStopSequences.remove(stopSequence);
        return new ModelConfiguration(aiModel, maxTokenLimit, temperature, frequencyPenalty, presencePenalty, newStopSequences, logitBias);
    }

    public ModelConfiguration addLogitBias(String token, Double bias) {

        Map<String, Double> newLogitBias = new HashMap<>(this.logitBias);
        newLogitBias.put(token, bias);
        return new ModelConfiguration(aiModel, maxTokenLimit, temperature, frequencyPenalty, presencePenalty, stopSequences, newLogitBias);
    }

    public ModelConfiguration removeLogitBias(String token) {

        Map<String, Double> newLogitBias = new HashMap<>(this.logitBias);
        newLogitBias.remove(token);
        return new ModelConfiguration(aiModel, maxTokenLimit, temperature, frequencyPenalty, presencePenalty, stopSequences, newLogitBias);
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

}
