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
        var modelConfiguration = ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT4_MINI)
                .maxTokenLimit(100)
                .temperature(1.0)
                .frequencyPenalty(0.2)
                .presencePenalty(0.2)
                .stopSequences(stopSequences)
                .logitBias(logitBias)
                .build();

        // Then
        assertThat(modelConfiguration).isNotNull();
        assertThat(modelConfiguration.getAiModel()).isEqualTo(ArtificialIntelligenceModel.GPT4_MINI);
        assertThat(modelConfiguration.getFrequencyPenalty()).isEqualTo(0.2);
        assertThat(modelConfiguration.getPresencePenalty()).isEqualTo(0.2);
        assertThat(modelConfiguration.getMaxTokenLimit()).isEqualTo(100);
        assertThat(modelConfiguration.getTemperature()).isEqualTo(1.0);
    }

    @Test
    public void createModelConfiguration_whenStopSequencesNull_thenCreateModelConfigurations() {

        // Given
        Map<String, Double> logitBias = new HashMap<>();
        logitBias.put("ABC", 50.0);
        logitBias.put("DEF", 5.0);

        // When
        var modelConfiguration = ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT4_MINI)
                .maxTokenLimit(100)
                .temperature(1.0)
                .frequencyPenalty(0.2)
                .presencePenalty(0.2)
                .stopSequences(null)
                .logitBias(logitBias)
                .build();

        // Then
        assertThat(modelConfiguration).isNotNull();
        assertThat(modelConfiguration.getStopSequences()).isEmpty();
    }

    @Test
    public void updateAiModel() {

        // Given
        ArtificialIntelligenceModel newModel = ArtificialIntelligenceModel.GPT4_OMNI;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.updateAiModel(newModel);

        // Then
        assertThat(newModelConfiguration.getAiModel()).isEqualTo(newModel);
    }

    @Test
    public void updateMaxTokenLimit() {

        // Given
        Integer newTokenLimit = 700;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.updateMaxTokenLimit(newTokenLimit);

        // Then
        assertThat(newModelConfiguration.getMaxTokenLimit()).isEqualTo(newTokenLimit);
    }

    @Test
    public void updateTemperature() {

        // Given
        Double newTemperature = 1.7;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.updateTemperature(newTemperature);

        // Then
        assertThat(newModelConfiguration.getTemperature()).isEqualTo(newTemperature);
    }

    @Test
    public void updateFrequencyPenalty() {

        // Given
        Double newFrequencyPenalty = 1.7;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.updateFrequencyPenalty(newFrequencyPenalty);

        // Then
        assertThat(newModelConfiguration.getFrequencyPenalty()).isEqualTo(newFrequencyPenalty);
    }

    @Test
    public void updateFrequencyPenalty_whenFrequencyPenaltyNull_thenUpdateWithDefault() {

        // Given
        Double defaultFrequencyPenalty = 0.0;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.updateFrequencyPenalty(null);

        // Then
        assertThat(newModelConfiguration.getFrequencyPenalty()).isEqualTo(defaultFrequencyPenalty);
    }

    @Test
    public void updatePresencePenalty() {

        // Given
        Double newPresencePenalty = 1.7;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.updatePresencePenalty(newPresencePenalty);

        // Then
        assertThat(newModelConfiguration.getPresencePenalty()).isEqualTo(newPresencePenalty);
    }

    @Test
    public void updatePresencePenalty_whenPresencePenaltyNull_thenUpdateWithDefault() {

        // Given
        Double defaultPresencePenalty = 0.0;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.updatePresencePenalty(null);

        // Then
        assertThat(newModelConfiguration.getPresencePenalty()).isEqualTo(defaultPresencePenalty);
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
        assertThat(newModelConfiguration.getLogitBias()).containsKey(newToken);
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
        assertThat(newModelConfiguration.getLogitBias()).doesNotContainKey(newToken);
    }

    @Test
    public void addStopSequence() {

        // Given
        String newToken = "323";
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.addStopSequence(newToken);

        // Then
        assertThat(newModelConfiguration.getStopSequences()).contains(newToken);
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
        assertThat(newModelConfiguration.getStopSequences())
                .isNotNull()
                .isNotEmpty()
                .doesNotContain(newToken);
    }

    @Test
    public void errorWhenTemperatureIsHigherThanLimit() {

        // Given
        double temperature = 3.0;

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT4_MINI)
                .maxTokenLimit(100)
                .temperature(temperature)
                .frequencyPenalty(0.2)
                .presencePenalty(0.2)
                .stopSequences(new HashSet<>())
                .logitBias(new HashMap<>())
                .build());
    }

    @Test
    public void errorWhenTemperatureIsLowerThanLimit() {

        // Given
        double temperature = -3.0;

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT4_MINI)
                .maxTokenLimit(100)
                .temperature(temperature)
                .frequencyPenalty(0.2)
                .presencePenalty(0.2)
                .stopSequences(new HashSet<>())
                .logitBias(new HashMap<>())
                .build());
    }

    @Test
    public void errorWhenMaxTokenLimitIsLowerThanLimit() {

        // Given
        int maxTokenLimit = 5;

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT4_MINI)
                .maxTokenLimit(maxTokenLimit)
                .temperature(1.0)
                .frequencyPenalty(0.2)
                .presencePenalty(0.2)
                .stopSequences(new HashSet<>())
                .logitBias(new HashMap<>())
                .build());
    }

    @Test
    public void errorWhenMaxTokenLimitIsHigherThanModelLimit() {

        // Given
        int gpt4MiniMaxTokenLimit = 200000;
        int gpt4OmniMaxTokenLimit = 500000;

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT4_MINI)
                .maxTokenLimit(gpt4MiniMaxTokenLimit)
                .temperature(1.0)
                .frequencyPenalty(0.2)
                .presencePenalty(0.2)
                .stopSequences(new HashSet<>())
                .logitBias(new HashMap<>())
                .build());
        assertThrows(BusinessRuleViolationException.class, () -> ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT4_OMNI)
                .maxTokenLimit(gpt4OmniMaxTokenLimit)
                .temperature(1.0)
                .frequencyPenalty(0.2)
                .presencePenalty(0.2)
                .stopSequences(new HashSet<>())
                .logitBias(new HashMap<>())
                .build());
    }

    @Test
    public void errorWhenLogitBiasIsLowerThanLimit() {

        // Given
        Map<String, Double> logitBias = new HashMap<>();
        logitBias.put("ABC", -200.0);

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT4_MINI)
                .maxTokenLimit(100)
                .temperature(1.0)
                .frequencyPenalty(0.2)
                .presencePenalty(0.2)
                .stopSequences(new HashSet<>())
                .logitBias(logitBias)
                .build());
    }

    @Test
    public void errorWhenLogitBiasIsHigherThanLimit() {

        // Given
        Map<String, Double> logitBias = new HashMap<>();
        logitBias.put("ABC", 200.0);

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT4_MINI)
                .maxTokenLimit(100)
                .temperature(1.0)
                .frequencyPenalty(0.2)
                .presencePenalty(0.2)
                .stopSequences(new HashSet<>())
                .logitBias(logitBias)
                .build());
    }

    @Test
    public void createModelConfigurationWithEmptyLogitBias() {

        // Given
        Map<String, Double> logitBias = Collections.emptyMap();

        // When
        var modelConfiguration = ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT4_MINI)
                .maxTokenLimit(100)
                .temperature(1.0)
                .frequencyPenalty(0.2)
                .presencePenalty(0.2)
                .stopSequences(new HashSet<>())
                .logitBias(logitBias)
                .build();

        // Then
        assertThat(modelConfiguration).isNotNull();
        assertThat(modelConfiguration.getLogitBias()).isNotNull().isEmpty();
    }

    @Test
    public void createModelConfigurationWithNullLogitBias() {

        // Given
        Map<String, Double> logitBias = null;

        // When
        var modelConfiguration = ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT4_MINI)
                .maxTokenLimit(100)
                .temperature(1.0)
                .frequencyPenalty(0.2)
                .presencePenalty(0.2)
                .stopSequences(new HashSet<>())
                .logitBias(logitBias)
                .build();

        // Then
        assertThat(modelConfiguration).isNotNull();
        assertThat(modelConfiguration.getLogitBias()).isNotNull().isEmpty();
    }

    @Test
    public void createModelConfigurationWithNullFrequencyPenalty() {

        // Given
        Double expectedFrequencyPenalty = 0.0;

        // When
        var modelConfiguration = ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT4_MINI)
                .maxTokenLimit(100)
                .temperature(1.0)
                .frequencyPenalty(null)
                .presencePenalty(0.2)
                .stopSequences(new HashSet<>())
                .logitBias(new HashMap<>())
                .build();

        // Then
        assertThat(modelConfiguration).isNotNull();
        assertThat(modelConfiguration.getFrequencyPenalty()).isNotNull().isEqualTo(expectedFrequencyPenalty);
    }

    @Test
    public void createModelConfigurationWithNullPresencePenalty() {

        // Given
        Double expectedPresencePenalty = 0.0;

        // When
        var modelConfiguration = ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT4_MINI)
                .maxTokenLimit(100)
                .temperature(1.0)
                .frequencyPenalty(0.2)
                .presencePenalty(null)
                .stopSequences(new HashSet<>())
                .logitBias(new HashMap<>())
                .build();

        // Then
        assertThat(modelConfiguration).isNotNull();
        assertThat(modelConfiguration.getPresencePenalty()).isNotNull().isEqualTo(expectedPresencePenalty);
    }

    @Test
    public void errorWhenFrequencyPenaltyIsHigherThanLimit() {

        // Given
        double frequencyPenalty = 3.0;

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT4_MINI)
                .maxTokenLimit(100)
                .temperature(1.0)
                .frequencyPenalty(frequencyPenalty)
                .presencePenalty(0.2)
                .stopSequences(new HashSet<>())
                .logitBias(new HashMap<>())
                .build());
    }

    @Test
    public void errorWhenFrequencyPenaltyIsLowerThanLimit() {

        // Given
        double frequencyPenalty = -3.0;

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT4_MINI)
                .maxTokenLimit(100)
                .temperature(1.0)
                .frequencyPenalty(frequencyPenalty)
                .presencePenalty(0.2)
                .stopSequences(new HashSet<>())
                .logitBias(new HashMap<>())
                .build());
    }

    @Test
    public void errorWhenPresencePenaltyIsHigherThanLimit() {

        // Given
        double presencePenalty = 3.0;

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT4_MINI)
                .maxTokenLimit(100)
                .temperature(1.0)
                .frequencyPenalty(0.2)
                .presencePenalty(presencePenalty)
                .stopSequences(new HashSet<>())
                .logitBias(new HashMap<>())
                .build());
    }

    @Test
    public void errorWhenPresencePenaltyIsLowerThanLimit() {

        // Given
        double presencePenalty = -3.0;

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT4_MINI)
                .maxTokenLimit(100)
                .temperature(1.0)
                .frequencyPenalty(0.2)
                .presencePenalty(presencePenalty)
                .stopSequences(new HashSet<>())
                .logitBias(new HashMap<>())
                .build());
    }
}
