package me.moirai.storyengine.core.domain.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

public class ModelConfigurationTest {

    @Test
    public void createModelConfiguration() {

        // Given
        Map<String, Double> logitBias = new HashMap<>();
        logitBias.put("ABC", 50.0);
        logitBias.put("DEF", 5.0);

        Set<String> stopSequences = new HashSet<>();
        stopSequences.add("ABC");

        // When
        ModelConfiguration modelConfiguration = new ModelConfiguration(
                ArtificialIntelligenceModel.GPT4_MINI,
                100,
                1.0,
                0.2,
                0.2,
                stopSequences,
                logitBias);

        // Then
        assertThat(modelConfiguration).isNotNull();
        assertThat(modelConfiguration.aiModel()).isEqualTo(ArtificialIntelligenceModel.GPT4_MINI);
        assertThat(modelConfiguration.frequencyPenalty()).isEqualTo(0.2);
        assertThat(modelConfiguration.presencePenalty()).isEqualTo(0.2);
        assertThat(modelConfiguration.maxTokenLimit()).isEqualTo(100);
        assertThat(modelConfiguration.temperature()).isEqualTo(1.0);
    }

    @Test
    public void createModelConfiguration_whenStopSequencesNull_thenCreateModelConfigurations() {

        // Given
        Map<String, Double> logitBias = new HashMap<>();
        logitBias.put("ABC", 50.0);
        logitBias.put("DEF", 5.0);

        // When
        ModelConfiguration modelConfiguration = new ModelConfiguration(
                ArtificialIntelligenceModel.GPT4_MINI,
                100,
                1.0,
                0.2,
                0.2,
                null,
                logitBias);

        // Then
        assertThat(modelConfiguration).isNotNull();
        assertThat(modelConfiguration.stopSequences()).isEmpty();
    }

    @Test
    public void updateAiModel() {

        // Given
        ArtificialIntelligenceModel newModel = ArtificialIntelligenceModel.GPT4_OMNI;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.updateAiModel(newModel);

        // Then
        assertThat(newModelConfiguration.aiModel()).isEqualTo(newModel);
    }

    @Test
    public void updateMaxTokenLimit() {

        // Given
        Integer newTokenLimit = 700;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.updateMaxTokenLimit(newTokenLimit);

        // Then
        assertThat(newModelConfiguration.maxTokenLimit()).isEqualTo(newTokenLimit);
    }

    @Test
    public void updateTemperature() {

        // Given
        Double newTemperature = 1.7;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.updateTemperature(newTemperature);

        // Then
        assertThat(newModelConfiguration.temperature()).isEqualTo(newTemperature);
    }

    @Test
    public void updateFrequencyPenalty() {

        // Given
        Double newFrequencyPenalty = 1.7;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.updateFrequencyPenalty(newFrequencyPenalty);

        // Then
        assertThat(newModelConfiguration.frequencyPenalty()).isEqualTo(newFrequencyPenalty);
    }

    @Test
    public void updateFrequencyPenalty_whenFrequencyPenaltyNull_thenUpdateWithDefault() {

        // Given
        Double defaultFrequencyPenalty = 0.0;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.updateFrequencyPenalty(null);

        // Then
        assertThat(newModelConfiguration.frequencyPenalty()).isEqualTo(defaultFrequencyPenalty);
    }

    @Test
    public void updatePresencePenalty() {

        // Given
        Double newPresencePenalty = 1.7;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.updatePresencePenalty(newPresencePenalty);

        // Then
        assertThat(newModelConfiguration.presencePenalty()).isEqualTo(newPresencePenalty);
    }

    @Test
    public void updatePresencePenalty_whenPresencePenaltyNull_thenUpdateWithDefault() {

        // Given
        Double defaultPresencePenalty = 0.0;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.updatePresencePenalty(null);

        // Then
        assertThat(newModelConfiguration.presencePenalty()).isEqualTo(defaultPresencePenalty);
    }

    @Test
    public void addLogitBias() {

        // Given
        String newToken = "323";
        Double bias = 57.0;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.addLogitBias(newToken, bias);

        // Then
        assertThat(newModelConfiguration.logitBias()).containsKey(newToken);
    }

    @Test
    public void removeLogitBias() {

        // Given
        String newToken = "323";
        Double bias = 57.0;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini();
        modelConfiguration = modelConfiguration.addLogitBias(newToken, bias);

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.removeLogitBias(newToken);

        // Then
        assertThat(newModelConfiguration.logitBias()).doesNotContainKey(newToken);
    }

    @Test
    public void addStopSequence() {

        // Given
        String newToken = "323";
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.addStopSequence(newToken);

        // Then
        assertThat(newModelConfiguration.stopSequences()).contains(newToken);
    }

    @Test
    public void removeStopSequence() {

        // Given
        String newToken = "323";
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini();
        modelConfiguration = modelConfiguration.addStopSequence(newToken);

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.removeStopSequence(newToken);

        // Then
        assertThat(newModelConfiguration.stopSequences())
                .isNotNull()
                .isNotEmpty()
                .doesNotContain(newToken);
    }

    @Test
    public void errorWhenTemperatureIsHigherThanLimit() {

        // Given
        double temperature = 3.0;

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> new ModelConfiguration(
                ArtificialIntelligenceModel.GPT4_MINI, 100, temperature, 0.2, 0.2,
                new HashSet<>(), new HashMap<>()));
    }

    @Test
    public void errorWhenTemperatureIsLowerThanLimit() {

        // Given
        double temperature = -3.0;

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> new ModelConfiguration(
                ArtificialIntelligenceModel.GPT4_MINI, 100, temperature, 0.2, 0.2,
                new HashSet<>(), new HashMap<>()));
    }

    @Test
    public void errorWhenMaxTokenLimitIsLowerThanLimit() {

        // Given
        int maxTokenLimit = 5;

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> new ModelConfiguration(
                ArtificialIntelligenceModel.GPT4_MINI, maxTokenLimit, 1.0, 0.2, 0.2,
                new HashSet<>(), new HashMap<>()));
    }

    @Test
    public void errorWhenMaxTokenLimitIsHigherThanModelLimit() {

        // Given
        int gpt4MiniMaxTokenLimit = 200000;
        int gpt4OmniMaxTokenLimit = 500000;

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> new ModelConfiguration(
                ArtificialIntelligenceModel.GPT4_MINI, gpt4MiniMaxTokenLimit, 1.0, 0.2, 0.2,
                new HashSet<>(), new HashMap<>()));
        assertThrows(BusinessRuleViolationException.class, () -> new ModelConfiguration(
                ArtificialIntelligenceModel.GPT4_OMNI, gpt4OmniMaxTokenLimit, 1.0, 0.2, 0.2,
                new HashSet<>(), new HashMap<>()));
    }

    @Test
    public void errorWhenLogitBiasIsLowerThanLimit() {

        // Given
        Map<String, Double> logitBias = new HashMap<>();
        logitBias.put("ABC", -200.0);

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> new ModelConfiguration(
                ArtificialIntelligenceModel.GPT4_MINI, 100, 1.0, 0.2, 0.2,
                new HashSet<>(), logitBias));
    }

    @Test
    public void errorWhenLogitBiasIsHigherThanLimit() {

        // Given
        Map<String, Double> logitBias = new HashMap<>();
        logitBias.put("ABC", 200.0);

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> new ModelConfiguration(
                ArtificialIntelligenceModel.GPT4_MINI, 100, 1.0, 0.2, 0.2,
                new HashSet<>(), logitBias));
    }

    @Test
    public void createModelConfigurationWithEmptyLogitBias() {

        // Given
        Map<String, Double> logitBias = Collections.emptyMap();

        // When
        ModelConfiguration modelConfiguration = new ModelConfiguration(
                ArtificialIntelligenceModel.GPT4_MINI, 100, 1.0, 0.2, 0.2,
                new HashSet<>(), logitBias);

        // Then
        assertThat(modelConfiguration).isNotNull();
        assertThat(modelConfiguration.logitBias()).isNotNull().isEmpty();
    }

    @Test
    public void createModelConfigurationWithNullLogitBias() {

        // Given
        Map<String, Double> logitBias = null;

        // When
        ModelConfiguration modelConfiguration = new ModelConfiguration(
                ArtificialIntelligenceModel.GPT4_MINI, 100, 1.0, 0.2, 0.2,
                new HashSet<>(), logitBias);

        // Then
        assertThat(modelConfiguration).isNotNull();
        assertThat(modelConfiguration.logitBias()).isNotNull().isEmpty();
    }

    @Test
    public void createModelConfigurationWithNullFrequencyPenalty() {

        // Given
        Double expectedFrequencyPenalty = 0.0;

        // When
        ModelConfiguration modelConfiguration = new ModelConfiguration(
                ArtificialIntelligenceModel.GPT4_MINI, 100, 1.0, null, 0.2,
                new HashSet<>(), new HashMap<>());

        // Then
        assertThat(modelConfiguration).isNotNull();
        assertThat(modelConfiguration.frequencyPenalty()).isNotNull().isEqualTo(expectedFrequencyPenalty);
    }

    @Test
    public void createModelConfigurationWithNullPresencePenalty() {

        // Given
        Double expectedPresencePenalty = 0.0;

        // When
        ModelConfiguration modelConfiguration = new ModelConfiguration(
                ArtificialIntelligenceModel.GPT4_MINI, 100, 1.0, 0.2, null,
                new HashSet<>(), new HashMap<>());

        // Then
        assertThat(modelConfiguration).isNotNull();
        assertThat(modelConfiguration.presencePenalty()).isNotNull().isEqualTo(expectedPresencePenalty);
    }

    @Test
    public void errorWhenFrequencyPenaltyIsHigherThanLimit() {

        // Given
        double frequencyPenalty = 3.0;

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> new ModelConfiguration(
                ArtificialIntelligenceModel.GPT4_MINI, 100, 1.0, frequencyPenalty, 0.2,
                new HashSet<>(), new HashMap<>()));
    }

    @Test
    public void errorWhenFrequencyPenaltyIsLowerThanLimit() {

        // Given
        double frequencyPenalty = -3.0;

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> new ModelConfiguration(
                ArtificialIntelligenceModel.GPT4_MINI, 100, 1.0, frequencyPenalty, 0.2,
                new HashSet<>(), new HashMap<>()));
    }

    @Test
    public void errorWhenPresencePenaltyIsHigherThanLimit() {

        // Given
        double presencePenalty = 3.0;

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> new ModelConfiguration(
                ArtificialIntelligenceModel.GPT4_MINI, 100, 1.0, 0.2, presencePenalty,
                new HashSet<>(), new HashMap<>()));
    }

    @Test
    public void errorWhenPresencePenaltyIsLowerThanLimit() {

        // Given
        double presencePenalty = -3.0;

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> new ModelConfiguration(
                ArtificialIntelligenceModel.GPT4_MINI, 100, 1.0, 0.2, presencePenalty,
                new HashSet<>(), new HashMap<>()));
    }
}
