package me.moirai.storyengine.core.application.usecase.adventure;

import java.util.UUID;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureByChannelId;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@UseCaseHandler
public class GetAdventureByChannelIdHandler
        extends AbstractUseCaseHandler<GetAdventureByChannelId, AdventureDetails> {

    private static final String ADVENTURE_NOT_FOUND = "No adventures exist for this channel";
    private static final String USER_NO_PERMISSION = "User does not have permission to view adventure";
    private static final String PERSONA_NOT_FOUND = "Persona not found";
    private static final String WORLD_NOT_FOUND = "World not found";

    private final AdventureRepository queryRepository;
    private final PersonaRepository personaRepository;
    private final WorldRepository worldRepository;

    public GetAdventureByChannelIdHandler(AdventureRepository queryRepository, PersonaRepository personaRepository, WorldRepository worldRepository) {
        this.queryRepository = queryRepository;
        this.personaRepository = personaRepository;
        this.worldRepository = worldRepository;
    }

    @Override
    public AdventureDetails execute(GetAdventureByChannelId useCase) {

        Adventure adventure = queryRepository.findByChannelId(useCase.getChannelId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_NOT_FOUND));

        if (!adventure.canUserRead(useCase.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION);
        }

        Persona persona = personaRepository.findById(adventure.getPersonaId())
                .orElseThrow(() -> new AssetNotFoundException(PERSONA_NOT_FOUND));

        World world = worldRepository.findById(adventure.getWorldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_NOT_FOUND));

        return toResult(adventure, persona.getPublicId(), world.getPublicId());
    }

    private AdventureDetails toResult(Adventure adventure, UUID personaPublicId, UUID worldPublicId) {

        return new AdventureDetails(
                adventure.getPublicId(),
                adventure.getName(),
                adventure.getDescription(),
                adventure.getAdventureStart(),
                worldPublicId,
                personaPublicId,
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
