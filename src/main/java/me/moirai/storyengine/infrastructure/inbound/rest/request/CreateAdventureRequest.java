package me.moirai.storyengine.infrastructure.inbound.rest.request;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateAdventureRequest(
        @NotEmpty(message = "cannot be empty") String name,
        @NotNull(message = "cannot be null") UUID worldId,
        @NotNull(message = "cannot be null") UUID personaId,
        @NotEmpty(message = "cannot be empty") String channelId,
        @NotEmpty(message = "cannot be empty") String visibility,
        @NotEmpty(message = "cannot be empty") String aiModel,
        @NotEmpty(message = "cannot be empty") String moderation,
        @NotNull(message = "cannot be null") @Min(value = 100, message = "cannot be less than 100") Integer maxTokenLimit,
        @NotNull(message = "cannot be null") @DecimalMin(value = "0.1", message = "cannot be less than 0.1") @DecimalMax(value = "2", message = "cannot be greater than 2") Double temperature,
        @DecimalMin(value = "-2", message = "cannot be less than -2") @DecimalMax(value = "2", message = "cannot be greater than 2") Double frequencyPenalty,
        @DecimalMin(value = "-2", message = "cannot be less than -2") @DecimalMax(value = "2", message = "cannot be greater than 2") Double presencePenalty,
        String gameMode,
        boolean isMultiplayer,
        Set<String> stopSequences,
        Map<String, Double> logitBias,
        Set<String> usersAllowedToWrite,
        Set<String> usersAllowedToRead,
        String nudge,
        String authorsNote,
        String remember,
        String bump,
        Integer bumpFrequency) {
}
