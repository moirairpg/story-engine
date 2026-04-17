package me.moirai.storyengine.core.domain.adventure;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

@Embeddable
public final class ModelConfiguration {

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_model")
    private ArtificialIntelligenceModel aiModel;

    @Column(name = "max_token_limit")
    private int maxTokenLimit;

    @Column(name = "temperature")
    private Double temperature;

    private ModelConfiguration(Builder builder) {

        this.aiModel = builder.aiModel;
        this.maxTokenLimit = builder.maxTokenLimit;
        this.temperature = builder.temperature;
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
                .temperature(modelConfiguration.getTemperature());
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

    private static void validateTemperature(double temperature) {

        if (temperature < 0.1 || temperature > 2) {
            throw new BusinessRuleViolationException("Temperature value has to be between 0 and 2");
        }
    }

    private static void validateMaxTokenLimit(int maxTokenLimit, ArtificialIntelligenceModel aiModel) {

        if (maxTokenLimit < 100 || maxTokenLimit > aiModel.getHardTokenLimit()) {
            throw new BusinessRuleViolationException(
                    String.format("Max token limit has to be between 100 and %s", aiModel.getHardTokenLimit()));
        }
    }

    public static final class Builder {

        private ArtificialIntelligenceModel aiModel;
        private Integer maxTokenLimit;
        private Double temperature;

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

        public ModelConfiguration build() {

            validateTemperature(temperature);
            validateMaxTokenLimit(maxTokenLimit, aiModel);

            return new ModelConfiguration(this);
        }
    }
}
