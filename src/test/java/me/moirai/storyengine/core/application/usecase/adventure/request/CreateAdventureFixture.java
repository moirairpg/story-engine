package me.moirai.storyengine.core.application.usecase.adventure.request;

import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventure;

import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;

public class CreateAdventureFixture {

    public static CreateAdventure sample() {

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();
        return new CreateAdventure(
                adventure.getName(),
                adventure.getDescription(),
                WorldFixture.PUBLIC_ID,
                PersonaFixture.PUBLIC_ID,
                adventure.getChannelId(),
                "private",
                adventure.getModelConfiguration().aiModel().toString(),
                "strict",
                "RQSTRID",
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
                adventure.getModelConfiguration().logitBias(),
                adventure.getModelConfiguration().stopSequences(),
                adventure.getUsersAllowedToWrite(),
                adventure.getUsersAllowedToRead(),
                adventure.isMultiplayer());
    }

    public static CreateAdventure sampleWithRequesterId(String requesterId) {

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();
        return new CreateAdventure(
                adventure.getName(),
                adventure.getDescription(),
                WorldFixture.PUBLIC_ID,
                PersonaFixture.PUBLIC_ID,
                adventure.getChannelId(),
                "private",
                adventure.getModelConfiguration().aiModel().toString(),
                "strict",
                requesterId,
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
                adventure.getModelConfiguration().logitBias(),
                adventure.getModelConfiguration().stopSequences(),
                adventure.getUsersAllowedToWrite(),
                adventure.getUsersAllowedToRead(),
                adventure.isMultiplayer());
    }
}
