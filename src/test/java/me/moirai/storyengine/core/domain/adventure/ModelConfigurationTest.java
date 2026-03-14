package me.moirai.storyengine.core.domain.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

public class ModelConfigurationTest {

    @Test
    public void createModelConfiguration() {

        // Given
        ModelConfiguration.Builder modelConfigurationBuilder = ModelConfiguration.builder();
        modelConfigurationBuilder.aiModel(ArtificialIntelligenceModel.GPT4_MINI);
        modelConfigurationBuilder.frequencyPenalty(0.2);
        modelConfigurationBuilder.presencePenalty(0.2);
        modelConfigurationBuilder.maxTokenLimit(100);
        modelConfigurationBuilder.temperature(1.0);

        Map<String, Double> logitBias = new HashMap<>();
        logitBias.put("ABC", 50.0);
        logitBias.put("DEF", 5.0);

        Set<String> stopSequences = new HashSet<>();
        stopSequences.add("ABC");

        modelConfigurationBuilder.logitBias(logitBias);
        modelConfigurationBuilder.stopSequences(stopSequences);

        // When
        ModelConfiguration modelConfiguration = modelConfigurationBuilder.build();

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
        ModelConfiguration.Builder modelConfigurationBuilder = ModelConfiguration.builder();
        modelConfigurationBuilder.aiModel(ArtificialIntelligenceModel.GPT4_MINI);
        modelConfigurationBuilder.frequencyPenalty(0.2);
        modelConfigurationBuilder.presencePenalty(0.2);
        modelConfigurationBuilder.maxTokenLimit(100);
        modelConfigurationBuilder.temperature(1.0);

        Map<String, Double> logitBias = new HashMap<>();
        logitBias.put("ABC", 50.0);
        logitBias.put("DEF", 5.0);

        List<String> stopSequences = new ArrayList<>();
        stopSequences.add("ABC");

        modelConfigurationBuilder.logitBias(logitBias);
        modelConfigurationBuilder.stopSequences(null);

        // When
        ModelConfiguration modelConfiguration = modelConfigurationBuilder.build();

        // Then
        assertThat(modelConfiguration).isNotNull();
        assertThat(modelConfiguration.getStopSequences()).isEmpty();
    }

    @Test
    public void updateAiModel() {

        // Given
        ArtificialIntelligenceModel newModel = ArtificialIntelligenceModel.GPT4_OMNI;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini().build();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.updateAiModel(newModel);

        // Then
        assertThat(newModelConfiguration.getAiModel()).isEqualTo(newModel);
    }

    @Test
    public void updateMaxTokenLimit() {

        // Given
        Integer newTokenLimit = 700;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini().build();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.updateMaxTokenLimit(newTokenLimit);

        // Then
        assertThat(newModelConfiguration.getMaxTokenLimit()).isEqualTo(newTokenLimit);
    }

    @Test
    public void updateTemperature() {

        // Given
        Double newTemperature = 1.7;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini().build();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.updateTemperature(newTemperature);

        // Then
        assertThat(newModelConfiguration.getTemperature()).isEqualTo(newTemperature);
    }

    @Test
    public void updateFrequencyPenalty() {

        // Given
        Double newFrequencyPenalty = 1.7;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini().build();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.updateFrequencyPenalty(newFrequencyPenalty);

        // Then
        assertThat(newModelConfiguration.getFrequencyPenalty()).isEqualTo(newFrequencyPenalty);
    }

    @Test
    public void updateFrequencyPenalty_whenFrequencyPenaltyNull_thenUpdateWithDefault() {

        // Given
        Double defaultFrequencyPenalty = 0.0;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini().build();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.updateFrequencyPenalty(null);

        // Then
        assertThat(newModelConfiguration.getFrequencyPenalty()).isEqualTo(defaultFrequencyPenalty);
    }

    @Test
    public void updatePresencePenalty() {

        // Given
        Double newPresencePenalty = 1.7;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini().build();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.updatePresencePenalty(newPresencePenalty);

        // Then
        assertThat(newModelConfiguration.getPresencePenalty()).isEqualTo(newPresencePenalty);
    }

    @Test
    public void updatePresencePenalty_whenPresencePenaltyNull_thenUpdateWithDefault() {

        // Given
        Double defaultPresencePenalty = 0.0;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini().build();

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
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini().build();

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
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini().build();
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
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini().build();

        // When
        ModelConfiguration newModelConfiguration = modelConfiguration.addStopSequence(newToken);

        // Then
        assertThat(newModelConfiguration.getStopSequences()).contains(newToken);
    }

    @Test
    public void removeStopSequence() {

        // Given
        String newToken = "323";
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini().build();
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
        ModelConfiguration.Builder modelConfigurationBuilder = ModelConfigurationFixture.gpt4Mini()
                .temperature(3.0);

        // Then
        assertThrows(BusinessRuleViolationException.class, modelConfigurationBuilder::build);
    }

    @Test
    public void errorWhenTemperatureIsLowerThanLimit() {

        // Given
        ModelConfiguration.Builder modelConfigurationBuilder = ModelConfigurationFixture.gpt4Mini()
                .temperature(-3.0);

        // Then
        assertThrows(BusinessRuleViolationException.class, modelConfigurationBuilder::build);
    }

    @Test
    public void errorWhenMaxTokenLimitIsLowerThanLimit() {

        // Given
        ModelConfiguration.Builder modelConfigurationBuilder = ModelConfigurationFixture.gpt4Mini()
                .maxTokenLimit(5);

        // Then
        assertThrows(BusinessRuleViolationException.class, modelConfigurationBuilder::build);
    }

    @Test
    public void errorWhenMaxTokenLimitIsHigherThanModelLimit() {

        // Given
        ModelConfiguration.Builder gpt4Mini = ModelConfigurationFixture.gpt4Mini()
                .maxTokenLimit(200000);

        ModelConfiguration.Builder gpt4Omni = ModelConfigurationFixture.gpt4Omni()
                .maxTokenLimit(500000);

        // Then
        assertThrows(BusinessRuleViolationException.class, gpt4Mini::build);
        assertThrows(BusinessRuleViolationException.class, gpt4Omni::build);
    }

    @Test
    public void errorWhenLogitBiasIsLowerThanLimit() {

        // Given
        Map<String, Double> logitBias = new HashMap<>();
        logitBias.put("ABC", -200.0);

        ModelConfiguration.Builder modelConfigurationBuilder = ModelConfigurationFixture.gpt4Mini()
                .logitBias(logitBias);

        // Then
        assertThrows(BusinessRuleViolationException.class, modelConfigurationBuilder::build);
    }

    @Test
    public void errorWhenLogitBiasIsHigherThanLimit() {

        // Given
        Map<String, Double> logitBias = new HashMap<>();
        logitBias.put("ABC", 200.0);

        ModelConfiguration.Builder modelConfigurationBuilder = ModelConfigurationFixture.gpt4Mini()
                .logitBias(logitBias);

        // Then
        assertThrows(BusinessRuleViolationException.class, modelConfigurationBuilder::build);
    }

    @Test
    public void createModelConfigurationWithEmptyLogitBias() {

        // Given
        Map<String, Double> logitBias = Collections.emptyMap();

        ModelConfiguration.Builder modelConfigurationBuilder = ModelConfigurationFixture.gpt4Mini()
                .logitBias(logitBias);

        // When
        ModelConfiguration modelConfiguration = modelConfigurationBuilder.build();

        // Then
        assertThat(modelConfiguration).isNotNull();
        assertThat(modelConfiguration.getLogitBias()).isNotNull().isEmpty();
    }

    @Test
    public void createModelConfigurationWithNullLogitBias() {

        // Given
        ModelConfiguration.Builder modelConfigurationBuilder = ModelConfiguration.builder();
        modelConfigurationBuilder.aiModel(ArtificialIntelligenceModel.GPT4_MINI);
        modelConfigurationBuilder.frequencyPenalty(0.2);
        modelConfigurationBuilder.presencePenalty(0.2);
        modelConfigurationBuilder.maxTokenLimit(100);
        modelConfigurationBuilder.temperature(1.0);
        modelConfigurationBuilder.logitBias(null);

        // When
        ModelConfiguration modelConfiguration = modelConfigurationBuilder.build();

        // Then
        assertThat(modelConfiguration).isNotNull();
        assertThat(modelConfiguration.getLogitBias()).isNotNull().isEmpty();
    }

    @Test
    public void createModelConfigurationWithNullFrequencyPenalty() {

        // Given
        Double frequencyPenalty = null;
        Double expectedFrequencyPenalty = 0.0;

        ModelConfiguration.Builder modelConfigurationBuilder = ModelConfigurationFixture.gpt4Mini()
                .frequencyPenalty(frequencyPenalty);

        // When
        ModelConfiguration modelConfiguration = modelConfigurationBuilder.build();

        // Then
        assertThat(modelConfiguration).isNotNull();
        assertThat(modelConfiguration.getFrequencyPenalty()).isNotNull().isEqualTo(expectedFrequencyPenalty);
    }

    @Test
    public void createModelConfigurationWithNullPresencePenalty() {

        // Given
        Double presencePenalty = null;
        Double expectedPresencePenalty = 0.0;

        ModelConfiguration.Builder modelConfigurationBuilder = ModelConfigurationFixture.gpt4Mini()
                .presencePenalty(presencePenalty);

        // When
        ModelConfiguration modelConfiguration = modelConfigurationBuilder.build();

        // Then
        assertThat(modelConfiguration).isNotNull();
        assertThat(modelConfiguration.getPresencePenalty()).isNotNull().isEqualTo(expectedPresencePenalty);
    }

    @Test
    public void errorWhenFrequencyPenaltyIsHigherThanLimit() {

        // Given
        double frequencyPenalty = 3.0;

        ModelConfiguration.Builder modelConfigurationBuilder = ModelConfigurationFixture.gpt4Mini()
                .frequencyPenalty(frequencyPenalty);

        // Then
        assertThrows(BusinessRuleViolationException.class, modelConfigurationBuilder::build);
    }

    @Test
    public void errorWhenFrequencyPenaltyIsLowerThanLimit() {

        // Given
        double frequencyPenalty = -3.0;

        ModelConfiguration.Builder modelConfigurationBuilder = ModelConfigurationFixture.gpt4Mini()
                .frequencyPenalty(frequencyPenalty);

        // Then
        assertThrows(BusinessRuleViolationException.class, modelConfigurationBuilder::build);
    }

    @Test
    public void errorWhenPresencePenaltyIsHigherThanLimit() {

        // Given
        double presencePenalty = 3.0;

        ModelConfiguration.Builder modelConfigurationBuilder = ModelConfigurationFixture.gpt4Mini()
                .presencePenalty(presencePenalty);

        // Then
        assertThrows(BusinessRuleViolationException.class, modelConfigurationBuilder::build);
    }

    @Test
    public void errorWhenPresencePenaltyIsLowerThanLimit() {

        // Given
        double presencePenalty = -3.0;

        ModelConfiguration.Builder modelConfigurationBuilder = ModelConfigurationFixture.gpt4Mini()
                .presencePenalty(presencePenalty);

        // Then
        assertThrows(BusinessRuleViolationException.class, modelConfigurationBuilder::build);
    }
}
