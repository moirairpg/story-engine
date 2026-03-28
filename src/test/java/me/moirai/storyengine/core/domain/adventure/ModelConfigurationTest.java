package me.moirai.storyengine.core.domain.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

public class ModelConfigurationTest {

    @Test
    public void createModelConfiguration() {

        // given
        // when
        var modelConfiguration = ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT54_MINI)
                .maxTokenLimit(100)
                .temperature(1.0)
                .build();

        // then
        assertThat(modelConfiguration).isNotNull();
        assertThat(modelConfiguration.getAiModel()).isEqualTo(ArtificialIntelligenceModel.GPT54_MINI);
        assertThat(modelConfiguration.getMaxTokenLimit()).isEqualTo(100);
        assertThat(modelConfiguration.getTemperature()).isEqualTo(1.0);
    }

    @Test
    public void updateAiModel() {

        // given
        var newModel = ArtificialIntelligenceModel.GPT54;
        var modelConfiguration = ModelConfigurationFixture.gpt4Mini();

        // when
        var newModelConfiguration = modelConfiguration.updateAiModel(newModel);

        // then
        assertThat(newModelConfiguration.getAiModel()).isEqualTo(newModel);
    }

    @Test
    public void updateMaxTokenLimit() {

        // given
        var newTokenLimit = 700;
        var modelConfiguration = ModelConfigurationFixture.gpt4Mini();

        // when
        var newModelConfiguration = modelConfiguration.updateMaxTokenLimit(newTokenLimit);

        // then
        assertThat(newModelConfiguration.getMaxTokenLimit()).isEqualTo(newTokenLimit);
    }

    @Test
    public void updateTemperature() {

        // given
        var newTemperature = 1.7;
        var modelConfiguration = ModelConfigurationFixture.gpt4Mini();

        // when
        var newModelConfiguration = modelConfiguration.updateTemperature(newTemperature);

        // then
        assertThat(newModelConfiguration.getTemperature()).isEqualTo(newTemperature);
    }

    @Test
    public void errorWhenTemperatureIsHigherThanLimit() {

        // given
        var temperature = 3.0;

        // then
        assertThrows(BusinessRuleViolationException.class, () -> ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT54_MINI)
                .maxTokenLimit(100)
                .temperature(temperature)
                .build());
    }

    @Test
    public void errorWhenTemperatureIsLowerThanLimit() {

        // given
        var temperature = -3.0;

        // then
        assertThrows(BusinessRuleViolationException.class, () -> ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT54_MINI)
                .maxTokenLimit(100)
                .temperature(temperature)
                .build());
    }

    @Test
    public void errorWhenMaxTokenLimitIsLowerThanLimit() {

        // given
        var maxTokenLimit = 5;

        // then
        assertThrows(BusinessRuleViolationException.class, () -> ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT54_MINI)
                .maxTokenLimit(maxTokenLimit)
                .temperature(1.0)
                .build());
    }

    @Test
    public void errorWhenMaxTokenLimitIsHigherThanModelLimit() {

        // given
        var gpt4MiniMaxTokenLimit = 500000;
        var gpt4OmniMaxTokenLimit = 1100000;

        // then
        assertThrows(BusinessRuleViolationException.class, () -> ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT54_MINI)
                .maxTokenLimit(gpt4MiniMaxTokenLimit)
                .temperature(1.0)
                .build());
        assertThrows(BusinessRuleViolationException.class, () -> ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT54)
                .maxTokenLimit(gpt4OmniMaxTokenLimit)
                .temperature(1.0)
                .build());
    }
}
