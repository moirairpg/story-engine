package me.moirai.storyengine.infrastructure.outbound.adapter.mapper;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRawDto;

@Component
public class AdventurePersistenceMapper {

    public AdventureRawDto mapToResult(Adventure adventure) {

        return new AdventureRawDto(
                adventure.getPublicId(),
                adventure.getName(),
                adventure.getDescription(),
                adventure.getAdventureStart(),
                adventure.getWorldId(),
                adventure.getPersonaId(),
                adventure.getChannelId(),
                adventure.getVisibility().name(),
                adventure.getModelConfiguration().aiModel().toString(),
                adventure.getModeration().name(),
                adventure.getGameMode().name(),
                adventure.getOwnerId(),
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().remember(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency(),
                adventure.getModelConfiguration().maxTokenLimit(),
                adventure.getModelConfiguration().temperature(),
                adventure.getModelConfiguration().frequencyPenalty(),
                adventure.getModelConfiguration().presencePenalty(),
                adventure.isMultiplayer(),
                adventure.getCreationDate(),
                adventure.getLastUpdateDate(),
                adventure.getModelConfiguration().logitBias(),
                adventure.getModelConfiguration().stopSequences(),
                adventure.getUsersAllowedToRead(),
                adventure.getUsersAllowedToWrite());
    }

}
