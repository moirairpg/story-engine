package me.moirai.storyengine.core.application.query.adventure;

import java.util.UUID;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.ContextAttributesDto;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureByChannelId;
import me.moirai.storyengine.core.port.inbound.adventure.ModelConfigurationDto;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@UseCaseHandler
public class GetAdventureByChannelIdHandler
        extends AbstractUseCaseHandler<GetAdventureByChannelId, AdventureDetails> {

    private static final String ADVENTURE_NOT_FOUND = "No adventures exist for this channel";
    private static final String PERSONA_NOT_FOUND = "Persona not found";
    private static final String WORLD_NOT_FOUND = "World not found";

    private final AdventureRepository queryRepository;
    private final PersonaRepository personaRepository;
    private final WorldRepository worldRepository;

    public GetAdventureByChannelIdHandler(
            AdventureRepository queryRepository,
            PersonaRepository personaRepository,
            WorldRepository worldRepository) {

        this.queryRepository = queryRepository;
        this.personaRepository = personaRepository;
        this.worldRepository = worldRepository;
    }

    @Override
    public AdventureDetails execute(GetAdventureByChannelId useCase) {

        var adventure = queryRepository.findByChannelId(useCase.getChannelId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_NOT_FOUND));

        var persona = personaRepository.findById(adventure.getPersonaId())
                .orElseThrow(() -> new AssetNotFoundException(PERSONA_NOT_FOUND));

        var world = worldRepository.findById(adventure.getWorldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_NOT_FOUND));

        return toResult(adventure, persona.getPublicId(), world.getPublicId());
    }

    private AdventureDetails toResult(Adventure adventure, UUID personaPublicId, UUID worldPublicId) {

        var modelConfiguration = new ModelConfigurationDto(
                adventure.getModelConfiguration().getAiModel(),
                adventure.getModelConfiguration().getMaxTokenLimit(),
                adventure.getModelConfiguration().getTemperature(),
                adventure.getModelConfiguration().getFrequencyPenalty(),
                adventure.getModelConfiguration().getPresencePenalty(),
                adventure.getModelConfiguration().getStopSequences(),
                adventure.getModelConfiguration().getLogitBias());

        var contextAttributes = new ContextAttributesDto(
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().remember(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency());

        return new AdventureDetails(
                adventure.getPublicId(),
                adventure.getName(),
                adventure.getDescription(),
                adventure.getAdventureStart(),
                worldPublicId,
                personaPublicId,
                adventure.getChannelId(),
                adventure.getVisibility().name(),
                adventure.getModeration().name(),
                adventure.getGameMode().name(),
                adventure.getOwnerId(),
                adventure.isMultiplayer(),
                adventure.getCreationDate(),
                adventure.getLastUpdateDate(),
                modelConfiguration,
                contextAttributes,
                adventure.getUsersAllowedToRead(),
                adventure.getUsersAllowedToWrite());
    }
}
