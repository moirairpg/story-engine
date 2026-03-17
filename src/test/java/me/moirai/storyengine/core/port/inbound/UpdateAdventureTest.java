package me.moirai.storyengine.core.port.inbound;

import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;

import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;

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
                adventure.getChannelId(),
                adventure.getVisibility(),
                adventure.getModelConfiguration().aiModel(),
                adventure.getModeration(),
                adventure.getOwnerId(),
                adventure.getGameMode(),
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().remember(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency(),
                adventure.getModelConfiguration().maxTokenLimit(),
                adventure.getModelConfiguration().temperature(),
                adventure.getModelConfiguration().frequencyPenalty(),
                adventure.getModelConfiguration().presencePenalty(),
                Maps.newHashMap("TKNID", 99D),
                adventure.getModelConfiguration().stopSequences(),
                adventure.getModelConfiguration().stopSequences(),
                Collections.singleton("TKN"),
                Collections.singleton("USRID"),
                Collections.singleton("USRID"),
                Collections.singleton("USRID"),
                Collections.singleton("USRID"),
                adventure.isMultiplayer());

        // Then
        assertThat(updateAdventure.adventureId()).isEqualTo(AdventureFixture.PUBLIC_ID);
        assertThat(updateAdventure.adventureStart()).isEqualTo(adventure.getAdventureStart());
        assertThat(updateAdventure.description()).isEqualTo(adventure.getDescription());
        assertThat(updateAdventure.channelId()).isEqualTo(adventure.getChannelId());
        assertThat(updateAdventure.gameMode()).isEqualTo(adventure.getGameMode());
        assertThat(updateAdventure.name()).isEqualTo(adventure.getName());
        assertThat(updateAdventure.personaId()).isEqualTo(PersonaFixture.PUBLIC_ID);
        assertThat(updateAdventure.worldId()).isEqualTo(WorldFixture.PUBLIC_ID);
        assertThat(updateAdventure.visibility()).isEqualTo(adventure.getVisibility());
        assertThat(updateAdventure.presencePenalty()).isEqualTo(adventure.getModelConfiguration().presencePenalty());
        assertThat(updateAdventure.frequencyPenalty()).isEqualTo(adventure.getModelConfiguration().frequencyPenalty());
        assertThat(updateAdventure.temperature()).isEqualTo(adventure.getModelConfiguration().temperature());
        assertThat(updateAdventure.maxTokenLimit()).isEqualTo(adventure.getModelConfiguration().maxTokenLimit());
        assertThat(updateAdventure.remember()).isEqualTo(adventure.getContextAttributes().remember());
        assertThat(updateAdventure.authorsNote()).isEqualTo(adventure.getContextAttributes().authorsNote());
        assertThat(updateAdventure.nudge()).isEqualTo(adventure.getContextAttributes().nudge());
        assertThat(updateAdventure.bump()).isEqualTo(adventure.getContextAttributes().bump());
        assertThat(updateAdventure.bumpFrequency()).isEqualTo(adventure.getContextAttributes().bumpFrequency());
        assertThat(updateAdventure.aiModel()).isEqualTo(adventure.getModelConfiguration().aiModel());
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
                sample.channelId(),
                sample.visibility(),
                sample.aiModel(),
                sample.moderation(),
                sample.requesterId(),
                sample.gameMode(),
                sample.nudge(),
                sample.remember(),
                sample.authorsNote(),
                sample.bump(),
                sample.bumpFrequency(),
                sample.maxTokenLimit(),
                sample.temperature(),
                sample.frequencyPenalty(),
                sample.presencePenalty(),
                sample.logitBiasToAdd(),
                null,
                sample.stopSequencesToRemove(),
                sample.logitBiasToRemove(),
                sample.usersAllowedToWriteToAdd(),
                sample.usersAllowedToWriteToRemove(),
                sample.usersAllowedToReadToAdd(),
                sample.usersAllowedToReadToRemove(),
                sample.isMultiplayer());

        // Then
        assertThat(updateAdventure.stopSequencesToAdd()).isNull();
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
                sample.channelId(),
                sample.visibility(),
                sample.aiModel(),
                sample.moderation(),
                sample.requesterId(),
                sample.gameMode(),
                sample.nudge(),
                sample.remember(),
                sample.authorsNote(),
                sample.bump(),
                sample.bumpFrequency(),
                sample.maxTokenLimit(),
                sample.temperature(),
                sample.frequencyPenalty(),
                sample.presencePenalty(),
                sample.logitBiasToAdd(),
                sample.stopSequencesToAdd(),
                null,
                sample.logitBiasToRemove(),
                sample.usersAllowedToWriteToAdd(),
                sample.usersAllowedToWriteToRemove(),
                sample.usersAllowedToReadToAdd(),
                sample.usersAllowedToReadToRemove(),
                sample.isMultiplayer());

        // Then
        assertThat(updateAdventure.stopSequencesToRemove()).isNull();
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
                sample.channelId(),
                sample.visibility(),
                sample.aiModel(),
                sample.moderation(),
                sample.requesterId(),
                sample.gameMode(),
                sample.nudge(),
                sample.remember(),
                sample.authorsNote(),
                sample.bump(),
                sample.bumpFrequency(),
                sample.maxTokenLimit(),
                sample.temperature(),
                sample.frequencyPenalty(),
                sample.presencePenalty(),
                null,
                sample.stopSequencesToAdd(),
                sample.stopSequencesToRemove(),
                sample.logitBiasToRemove(),
                sample.usersAllowedToWriteToAdd(),
                sample.usersAllowedToWriteToRemove(),
                sample.usersAllowedToReadToAdd(),
                sample.usersAllowedToReadToRemove(),
                sample.isMultiplayer());

        // Then
        assertThat(updateAdventure.logitBiasToAdd()).isNull();
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
                sample.channelId(),
                sample.visibility(),
                sample.aiModel(),
                sample.moderation(),
                sample.requesterId(),
                sample.gameMode(),
                sample.nudge(),
                sample.remember(),
                sample.authorsNote(),
                sample.bump(),
                sample.bumpFrequency(),
                sample.maxTokenLimit(),
                sample.temperature(),
                sample.frequencyPenalty(),
                sample.presencePenalty(),
                sample.logitBiasToAdd(),
                sample.stopSequencesToAdd(),
                sample.stopSequencesToRemove(),
                null,
                sample.usersAllowedToWriteToAdd(),
                sample.usersAllowedToWriteToRemove(),
                sample.usersAllowedToReadToAdd(),
                sample.usersAllowedToReadToRemove(),
                sample.isMultiplayer());

        // Then
        assertThat(updateAdventure.logitBiasToRemove()).isNull();
    }

    @Test
    public void updateAdventure_whenUsersAllowedToWriteToAddIsNull_thenListIsEmpty() {

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
                sample.channelId(),
                sample.visibility(),
                sample.aiModel(),
                sample.moderation(),
                sample.requesterId(),
                sample.gameMode(),
                sample.nudge(),
                sample.remember(),
                sample.authorsNote(),
                sample.bump(),
                sample.bumpFrequency(),
                sample.maxTokenLimit(),
                sample.temperature(),
                sample.frequencyPenalty(),
                sample.presencePenalty(),
                sample.logitBiasToAdd(),
                sample.stopSequencesToAdd(),
                sample.stopSequencesToRemove(),
                sample.logitBiasToRemove(),
                null,
                sample.usersAllowedToWriteToRemove(),
                sample.usersAllowedToReadToAdd(),
                sample.usersAllowedToReadToRemove(),
                sample.isMultiplayer());

        // Then
        assertThat(updateAdventure.usersAllowedToWriteToAdd()).isNull();
    }

    @Test
    public void updateAdventure_whenUsersAllowedToWriteToRemoveIsNull_thenListIsEmpty() {

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
                sample.channelId(),
                sample.visibility(),
                sample.aiModel(),
                sample.moderation(),
                sample.requesterId(),
                sample.gameMode(),
                sample.nudge(),
                sample.remember(),
                sample.authorsNote(),
                sample.bump(),
                sample.bumpFrequency(),
                sample.maxTokenLimit(),
                sample.temperature(),
                sample.frequencyPenalty(),
                sample.presencePenalty(),
                sample.logitBiasToAdd(),
                sample.stopSequencesToAdd(),
                sample.stopSequencesToRemove(),
                sample.logitBiasToRemove(),
                sample.usersAllowedToWriteToAdd(),
                null,
                sample.usersAllowedToReadToAdd(),
                sample.usersAllowedToReadToRemove(),
                sample.isMultiplayer());

        // Then
        assertThat(updateAdventure.usersAllowedToWriteToRemove()).isNull();
    }

    @Test
    public void updateAdventure_whenUsersAllowedToReadToAddIsNull_thenListIsEmpty() {

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
                sample.channelId(),
                sample.visibility(),
                sample.aiModel(),
                sample.moderation(),
                sample.requesterId(),
                sample.gameMode(),
                sample.nudge(),
                sample.remember(),
                sample.authorsNote(),
                sample.bump(),
                sample.bumpFrequency(),
                sample.maxTokenLimit(),
                sample.temperature(),
                sample.frequencyPenalty(),
                sample.presencePenalty(),
                sample.logitBiasToAdd(),
                sample.stopSequencesToAdd(),
                sample.stopSequencesToRemove(),
                sample.logitBiasToRemove(),
                sample.usersAllowedToWriteToAdd(),
                sample.usersAllowedToWriteToRemove(),
                null,
                sample.usersAllowedToReadToRemove(),
                sample.isMultiplayer());

        // Then
        assertThat(updateAdventure.usersAllowedToReadToAdd()).isNull();
    }

    @Test
    public void updateAdventure_whenUsersAllowedToReadToRemoveIsNull_thenListIsEmpty() {

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
                sample.channelId(),
                sample.visibility(),
                sample.aiModel(),
                sample.moderation(),
                sample.requesterId(),
                sample.gameMode(),
                sample.nudge(),
                sample.remember(),
                sample.authorsNote(),
                sample.bump(),
                sample.bumpFrequency(),
                sample.maxTokenLimit(),
                sample.temperature(),
                sample.frequencyPenalty(),
                sample.presencePenalty(),
                sample.logitBiasToAdd(),
                sample.stopSequencesToAdd(),
                sample.stopSequencesToRemove(),
                sample.logitBiasToRemove(),
                sample.usersAllowedToWriteToAdd(),
                sample.usersAllowedToWriteToRemove(),
                sample.usersAllowedToReadToAdd(),
                null,
                sample.isMultiplayer());

        // Then
        assertThat(updateAdventure.usersAllowedToReadToRemove()).isNull();
    }
}
