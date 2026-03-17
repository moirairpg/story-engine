package me.moirai.storyengine.core.application.usecase.adventure.request;

import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;

import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;

public class CreateAdventureTest {

    @Test
    public void updateAdventure_whenValidDate_thenInstanceIsCreated() {

        // Given
        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();

        // When
        CreateAdventure updateAdventure = new CreateAdventure(
                adventure.getName(),
                adventure.getDescription(),
                WorldFixture.PUBLIC_ID,
                PersonaFixture.PUBLIC_ID,
                adventure.getChannelId(),
                adventure.getVisibility().name(),
                adventure.getModelConfiguration().aiModel().toString(),
                adventure.getModeration().name(),
                adventure.getOwnerId(),
                adventure.getGameMode().name(),
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().remember(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency(),
                adventure.getModelConfiguration().maxTokenLimit(),
                1.7,
                adventure.getModelConfiguration().frequencyPenalty(),
                adventure.getModelConfiguration().presencePenalty(),
                Maps.newHashMap("TKNID", 99D),
                adventure.getModelConfiguration().stopSequences(),
                Collections.singleton("USRID"),
                Collections.singleton("USRID"),
                adventure.isMultiplayer());

        // Then
        assertThat(updateAdventure.description()).isEqualTo(adventure.getDescription());
        assertThat(updateAdventure.channelId()).isEqualTo(adventure.getChannelId());
        assertThat(updateAdventure.gameMode()).isEqualToIgnoringCase(adventure.getGameMode().name());
        assertThat(updateAdventure.name()).isEqualTo(adventure.getName());
        assertThat(updateAdventure.personaId()).isEqualTo(PersonaFixture.PUBLIC_ID);
        assertThat(updateAdventure.worldId()).isEqualTo(WorldFixture.PUBLIC_ID);
        assertThat(updateAdventure.visibility()).isEqualToIgnoringCase(adventure.getVisibility().name());
        assertThat(updateAdventure.presencePenalty())
                .isEqualTo(adventure.getModelConfiguration().presencePenalty());
        assertThat(updateAdventure.frequencyPenalty())
                .isEqualTo(adventure.getModelConfiguration().frequencyPenalty());
        assertThat(updateAdventure.temperature()).isEqualTo(1.7);
        assertThat(updateAdventure.maxTokenLimit()).isEqualTo(adventure.getModelConfiguration().maxTokenLimit());
        assertThat(updateAdventure.remember()).isEqualTo(adventure.getContextAttributes().remember());
        assertThat(updateAdventure.authorsNote()).isEqualTo(adventure.getContextAttributes().authorsNote());
        assertThat(updateAdventure.nudge()).isEqualTo(adventure.getContextAttributes().nudge());
        assertThat(updateAdventure.bump()).isEqualTo(adventure.getContextAttributes().bump());
        assertThat(updateAdventure.bumpFrequency()).isEqualTo(adventure.getContextAttributes().bumpFrequency());
        assertThat(updateAdventure.aiModel())
                .isEqualToIgnoringCase(adventure.getModelConfiguration().aiModel().toString());
    }

    @Test
    public void updateAdventure_whenStopSequencesIsNull_thenListIsEmpty() {

        // Given
        CreateAdventure sample = CreateAdventureFixture.sample();

        // When
        CreateAdventure updateAdventure = new CreateAdventure(
                sample.name(),
                sample.description(),
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
                sample.logitBias(),
                null,
                sample.usersAllowedToWrite(),
                sample.usersAllowedToRead(),
                sample.isMultiplayer());

        // Then
        assertThat(updateAdventure.stopSequences()).isNull();
    }

    @Test
    public void updateAdventure_whenLogitBiasIsNull_thenListIsEmpty() {

        // Given
        CreateAdventure sample = CreateAdventureFixture.sample();

        // When
        CreateAdventure updateAdventure = new CreateAdventure(
                sample.name(),
                sample.description(),
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
                sample.stopSequences(),
                sample.usersAllowedToWrite(),
                sample.usersAllowedToRead(),
                sample.isMultiplayer());

        // Then
        assertThat(updateAdventure.logitBias()).isNull();
    }

    @Test
    public void updateAdventure_whenUsersAllowedToWriteIsNull_thenListIsEmpty() {

        // Given
        CreateAdventure sample = CreateAdventureFixture.sample();

        // When
        CreateAdventure updateAdventure = new CreateAdventure(
                sample.name(),
                sample.description(),
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
                sample.logitBias(),
                sample.stopSequences(),
                null,
                sample.usersAllowedToRead(),
                sample.isMultiplayer());

        // Then
        assertThat(updateAdventure.usersAllowedToWrite()).isNull();
    }

    @Test
    public void updateAdventure_whenUsersAllowedToReadIsNull_thenListIsEmpty() {

        // Given
        CreateAdventure sample = CreateAdventureFixture.sample();

        // When
        CreateAdventure updateAdventure = new CreateAdventure(
                sample.name(),
                sample.description(),
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
                sample.logitBias(),
                sample.stopSequences(),
                sample.usersAllowedToWrite(),
                null,
                sample.isMultiplayer());

        // Then
        assertThat(updateAdventure.usersAllowedToRead()).isNull();
    }
}
