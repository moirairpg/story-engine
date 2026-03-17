package me.moirai.storyengine.core.application.usecase.adventure;

import java.util.UUID;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureById;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@QueryHandler
public class GetAdventureByIdHandler extends AbstractQueryHandler<GetAdventureById, AdventureDetails> {

    private static final String ADVENTURE_NOT_FOUND = "Adventure to be viewed was not found";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "Adventure ID cannot be null or empty";
    private static final String USER_NO_PERMISSION = "User does not have permission to view adventure";
    private static final String PERSONA_NOT_FOUND = "Persona not found";
    private static final String WORLD_NOT_FOUND = "World not found";

    private final AdventureRepository queryRepository;
    private final PersonaRepository personaRepository;
    private final WorldRepository worldRepository;

    public GetAdventureByIdHandler(AdventureRepository queryRepository, PersonaRepository personaRepository, WorldRepository worldRepository) {
        this.queryRepository = queryRepository;
        this.personaRepository = personaRepository;
        this.worldRepository = worldRepository;
    }

    @Override
    public void validate(GetAdventureById command) {

        if (command.adventureId() == null) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public AdventureDetails execute(GetAdventureById query) {

        Adventure adventure = queryRepository.findByPublicId(query.adventureId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_NOT_FOUND));

        if (!adventure.canUserRead(query.requesterId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION);
        }

        Persona persona = personaRepository.findById(adventure.getPersonaId())
                .orElseThrow(() -> new AssetNotFoundException(PERSONA_NOT_FOUND));

        World world = worldRepository.findById(adventure.getWorldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_NOT_FOUND));

        return mapResult(adventure, persona.getPublicId(), world.getPublicId());
    }

    private AdventureDetails mapResult(Adventure adventure, UUID personaPublicId, UUID worldPublicId) {

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
