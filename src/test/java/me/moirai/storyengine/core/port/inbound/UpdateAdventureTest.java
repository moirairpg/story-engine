package me.moirai.storyengine.core.port.inbound;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;

import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.adventure.ContextAttributesDto;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateModelConfigurationDto;

public class UpdateAdventureTest {

    @Test
    public void updateAdventure_whenValidDate_thenInstanceIsCreated() {

        // Given
        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();

        // When
        UpdateAdventure updateAdventure = new UpdateAdventure(
                AdventureFixture.PUBLIC_ID,
                adventure.getDescription(),
                adventure.getAdventureStart(),
                adventure.getName(),
                WorldFixture.PUBLIC_ID,
                PersonaFixture.PUBLIC_ID,
                adventure.getVisibility(),
                adventure.getModeration(),
                adventure.isMultiplayer(),
                Set.of(),
                new UpdateModelConfigurationDto(
                        adventure.getModelConfiguration().getAiModel(),
                        adventure.getModelConfiguration().getMaxTokenLimit(),
                        adventure.getModelConfiguration().getTemperature(),
                        adventure.getModelConfiguration().getFrequencyPenalty(),
                        adventure.getModelConfiguration().getPresencePenalty(),
                        adventure.getModelConfiguration().getStopSequences(),
                        adventure.getModelConfiguration().getStopSequences(),
                        Maps.newHashMap("TKNID", 99D),
                        Set.of("TKN")),
                new ContextAttributesDto(
                        adventure.getContextAttributes().nudge(),
                        adventure.getContextAttributes().authorsNote(),
                        adventure.getContextAttributes().scene(),
                        adventure.getContextAttributes().bump(),
                        adventure.getContextAttributes().bumpFrequency()));

        // Then
        assertThat(updateAdventure.adventureId()).isEqualTo(AdventureFixture.PUBLIC_ID);
        assertThat(updateAdventure.adventureStart()).isEqualTo(adventure.getAdventureStart());
        assertThat(updateAdventure.description()).isEqualTo(adventure.getDescription());
        assertThat(updateAdventure.name()).isEqualTo(adventure.getName());
        assertThat(updateAdventure.personaId()).isEqualTo(PersonaFixture.PUBLIC_ID);
        assertThat(updateAdventure.worldId()).isEqualTo(WorldFixture.PUBLIC_ID);
        assertThat(updateAdventure.visibility()).isEqualTo(adventure.getVisibility());
        assertThat(updateAdventure.modelConfiguration().presencePenalty()).isEqualTo(adventure.getModelConfiguration().getPresencePenalty());
        assertThat(updateAdventure.modelConfiguration().frequencyPenalty()).isEqualTo(adventure.getModelConfiguration().getFrequencyPenalty());
        assertThat(updateAdventure.modelConfiguration().temperature()).isEqualTo(adventure.getModelConfiguration().getTemperature());
        assertThat(updateAdventure.modelConfiguration().maxTokenLimit()).isEqualTo(adventure.getModelConfiguration().getMaxTokenLimit());
        assertThat(updateAdventure.contextAttributes().scene()).isEqualTo(adventure.getContextAttributes().scene());
        assertThat(updateAdventure.contextAttributes().authorsNote()).isEqualTo(adventure.getContextAttributes().authorsNote());
        assertThat(updateAdventure.contextAttributes().nudge()).isEqualTo(adventure.getContextAttributes().nudge());
        assertThat(updateAdventure.contextAttributes().bump()).isEqualTo(adventure.getContextAttributes().bump());
        assertThat(updateAdventure.contextAttributes().bumpFrequency()).isEqualTo(adventure.getContextAttributes().bumpFrequency());
        assertThat(updateAdventure.modelConfiguration().aiModel()).isEqualTo(adventure.getModelConfiguration().getAiModel());
    }

    @Test
    public void updateAdventure_whenStopSequencesToAddIsNull_thenListIsEmpty() {

        // Given
        UpdateAdventure sample = UpdateAdventureFixture.sample();

        // When
        UpdateAdventure updateAdventure = new UpdateAdventure(
                sample.adventureId(),
                sample.description(),
                sample.adventureStart(),
                sample.name(),
                sample.worldId(),
                sample.personaId(),
                sample.visibility(),
                sample.moderation(),
                sample.isMultiplayer(),
                sample.permissions(),
                new UpdateModelConfigurationDto(
                        sample.modelConfiguration().aiModel(),
                        sample.modelConfiguration().maxTokenLimit(),
                        sample.modelConfiguration().temperature(),
                        sample.modelConfiguration().frequencyPenalty(),
                        sample.modelConfiguration().presencePenalty(),
                        null,
                        sample.modelConfiguration().stopSequencesToRemove(),
                        sample.modelConfiguration().logitBiasToAdd(),
                        sample.modelConfiguration().logitBiasToRemove()),
                sample.contextAttributes());

        // Then
        assertThat(updateAdventure.modelConfiguration().stopSequencesToAdd()).isNull();
    }

    @Test
    public void updateAdventure_whenStopSequencesToRemoveIsNull_thenListIsEmpty() {

        // Given
        UpdateAdventure sample = UpdateAdventureFixture.sample();

        // When
        UpdateAdventure updateAdventure = new UpdateAdventure(
                sample.adventureId(),
                sample.description(),
                sample.adventureStart(),
                sample.name(),
                sample.worldId(),
                sample.personaId(),
                sample.visibility(),
                sample.moderation(),
                sample.isMultiplayer(),
                sample.permissions(),
                new UpdateModelConfigurationDto(
                        sample.modelConfiguration().aiModel(),
                        sample.modelConfiguration().maxTokenLimit(),
                        sample.modelConfiguration().temperature(),
                        sample.modelConfiguration().frequencyPenalty(),
                        sample.modelConfiguration().presencePenalty(),
                        sample.modelConfiguration().stopSequencesToAdd(),
                        null,
                        sample.modelConfiguration().logitBiasToAdd(),
                        sample.modelConfiguration().logitBiasToRemove()),
                sample.contextAttributes());

        // Then
        assertThat(updateAdventure.modelConfiguration().stopSequencesToRemove()).isNull();
    }

    @Test
    public void updateAdventure_whenLogitBiasToAddIsNull_thenListIsEmpty() {

        // Given
        UpdateAdventure sample = UpdateAdventureFixture.sample();

        // When
        UpdateAdventure updateAdventure = new UpdateAdventure(
                sample.adventureId(),
                sample.description(),
                sample.adventureStart(),
                sample.name(),
                sample.worldId(),
                sample.personaId(),
                sample.visibility(),
                sample.moderation(),
                sample.isMultiplayer(),
                sample.permissions(),
                new UpdateModelConfigurationDto(
                        sample.modelConfiguration().aiModel(),
                        sample.modelConfiguration().maxTokenLimit(),
                        sample.modelConfiguration().temperature(),
                        sample.modelConfiguration().frequencyPenalty(),
                        sample.modelConfiguration().presencePenalty(),
                        sample.modelConfiguration().stopSequencesToAdd(),
                        sample.modelConfiguration().stopSequencesToRemove(),
                        null,
                        sample.modelConfiguration().logitBiasToRemove()),
                sample.contextAttributes());

        // Then
        assertThat(updateAdventure.modelConfiguration().logitBiasToAdd()).isNull();
    }

    @Test
    public void updateAdventure_whenLogitBiasToRemoveIsNull_thenListIsEmpty() {

        // Given
        UpdateAdventure sample = UpdateAdventureFixture.sample();

        // When
        UpdateAdventure updateAdventure = new UpdateAdventure(
                sample.adventureId(),
                sample.description(),
                sample.adventureStart(),
                sample.name(),
                sample.worldId(),
                sample.personaId(),
                sample.visibility(),
                sample.moderation(),
                sample.isMultiplayer(),
                sample.permissions(),
                new UpdateModelConfigurationDto(
                        sample.modelConfiguration().aiModel(),
                        sample.modelConfiguration().maxTokenLimit(),
                        sample.modelConfiguration().temperature(),
                        sample.modelConfiguration().frequencyPenalty(),
                        sample.modelConfiguration().presencePenalty(),
                        sample.modelConfiguration().stopSequencesToAdd(),
                        sample.modelConfiguration().stopSequencesToRemove(),
                        sample.modelConfiguration().logitBiasToAdd(),
                        null),
                sample.contextAttributes());

        // Then
        assertThat(updateAdventure.modelConfiguration().logitBiasToRemove()).isNull();
    }

    @Test
    public void updateAdventure_whenPermissionsIsNull_thenSetIsEmpty() {

        // Given
        UpdateAdventure sample = UpdateAdventureFixture.sample();

        // When
        UpdateAdventure updateAdventure = new UpdateAdventure(
                sample.adventureId(),
                sample.description(),
                sample.adventureStart(),
                sample.name(),
                sample.worldId(),
                sample.personaId(),
                sample.visibility(),
                sample.moderation(),
                sample.isMultiplayer(),
                null,
                sample.modelConfiguration(),
                sample.contextAttributes());

        // Then
        assertThat(updateAdventure.permissions()).isEmpty();
    }
}
