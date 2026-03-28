package me.moirai.storyengine.infrastructure.inbound.rest.request;

import java.util.Map;
import java.util.Set;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;

public record UpdateModelConfigurationRequest(
        @NotNull(message = "cannot be empty") ArtificialIntelligenceModel aiModel,
        @NotNull(message = "cannot be null") @Min(value = 100, message = "cannot be less than 100") Integer maxTokenLimit,
        @NotNull(message = "cannot be null") @DecimalMin(value = "0.1", message = "cannot be less than 0.1") @DecimalMax(value = "2", message = "cannot be greater than 2") Double temperature,
        @DecimalMin(value = "-2", message = "cannot be less than -2") @DecimalMax(value = "2", message = "cannot be greater than 2") Double frequencyPenalty,
        @DecimalMin(value = "-2", message = "cannot be less than -2") @DecimalMax(value = "2", message = "cannot be greater than 2") Double presencePenalty,
        Set<String> stopSequencesToAdd,
        Set<String> stopSequencesToRemove,
        Map<String, Double> logitBiasToAdd,
        Set<String> logitBiasToRemove) {
}
