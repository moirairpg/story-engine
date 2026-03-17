package me.moirai.storyengine.core.application.command.adventure;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.collections4.MapUtils.emptyIfNull;

import java.util.UUID;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@CommandHandler
public class UpdateAdventureHandler extends AbstractCommandHandler<UpdateAdventure, AdventureDetails> {

    private static final String USER_NO_PERMISSION = "User does not have permission to delete adventure";
    private static final String ADVENTURE_NOT_FOUND = "Adventure to be updated was not found";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "Adventure ID cannot be null or empty";
    private static final String PERSONA_NOT_FOUND = "Persona not found";
    private static final String WORLD_NOT_FOUND = "World not found";

    private final AdventureRepository repository;
    private final PersonaRepository personaRepository;
    private final WorldRepository worldRepository;

    public UpdateAdventureHandler(AdventureRepository repository, PersonaRepository personaRepository,
            WorldRepository worldRepository) {
        this.repository = repository;
        this.personaRepository = personaRepository;
        this.worldRepository = worldRepository;
    }

    @Override
    public void validate(UpdateAdventure command) {

        if (command.adventureId() == null) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public AdventureDetails execute(UpdateAdventure command) {

        var adventure = repository.findByPublicId(command.adventureId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_NOT_FOUND));

        // TODO externalize to authorizer
        if (!adventure.canUserWrite(command.requesterId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION);
        }

        var persona = personaRepository.findByPublicId(command.personaId())
                .orElseThrow(() -> new AssetNotFoundException(PERSONA_NOT_FOUND));

        var world = worldRepository.findByPublicId(command.worldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_NOT_FOUND));

        adventure.updateName(command.name());
        adventure.updateWorld(world.getId());
        adventure.updatePersona(persona.getId());
        adventure.updateAiModel(command.aiModel());
        adventure.updateModeration(command.moderation());
        adventure.updateChannel(command.channelId());
        adventure.updateGameMode(command.gameMode());
        adventure.updateTemperature(command.temperature());
        adventure.updateFrequencyPenalty(command.frequencyPenalty());
        adventure.updatePresencePenalty(command.presencePenalty());
        adventure.updateAdventureStart(command.adventureStart());
        adventure.updateDescription(command.description());
        adventure.updateNudge(command.nudge());
        adventure.updateRemember(command.remember());
        adventure.updateAuthorsNote(command.authorsNote());
        adventure.updateBump(command.bump());
        adventure.updateBumpFrequency(command.bumpFrequency());

        if (command.isMultiplayer()) {
            adventure.makeMultiplayer();
        } else {
            adventure.makeSinglePlayer();
        }

        updateStopSequences(command, adventure);
        updateLogitBias(command, adventure);
        updatePermissions(command, adventure);

        var savedAdventure = repository.save(adventure);
        return mapResult(savedAdventure, persona.getPublicId(), world.getPublicId());
    }

    private void updatePermissions(UpdateAdventure command, Adventure adventure) {

        if (command.visibility() != null) {
            switch (command.visibility()) {
                case PUBLIC -> adventure.makePublic();
                case PRIVATE -> adventure.makePrivate();
                default -> adventure.makePrivate();
            }
        }

        emptyIfNull(command.usersAllowedToReadToAdd())
                .forEach(adventure::addReaderUser);

        emptyIfNull(command.usersAllowedToWriteToAdd())
                .forEach(adventure::addWriterUser);

        emptyIfNull(command.usersAllowedToReadToRemove())
                .forEach(adventure::removeReaderUser);

        emptyIfNull(command.usersAllowedToWriteToRemove())
                .forEach(adventure::removeWriterUser);
    }

    private void updateLogitBias(UpdateAdventure command, Adventure adventure) {

        emptyIfNull(command.logitBiasToAdd())
                .entrySet()
                .stream()
                .forEach(entry -> adventure.addLogitBias(entry.getKey(), entry.getValue()));

        emptyIfNull(command.logitBiasToRemove())
                .forEach(adventure::removeLogitBias);
    }

    private void updateStopSequences(UpdateAdventure command, Adventure adventure) {

        emptyIfNull(command.stopSequencesToAdd())
                .stream()
                .forEach(adventure::addStopSequence);

        emptyIfNull(command.stopSequencesToRemove())
                .forEach(adventure::removeStopSequence);
    }

    private AdventureDetails mapResult(Adventure savedAdventure, UUID personaPublicId, UUID worldPublicId) {

        return new AdventureDetails(
                savedAdventure.getPublicId(),
                savedAdventure.getName(),
                savedAdventure.getDescription(),
                savedAdventure.getAdventureStart(),
                worldPublicId,
                personaPublicId,
                savedAdventure.getChannelId(),
                savedAdventure.getVisibility().name(),
                savedAdventure.getModelConfiguration().aiModel().toString(),
                savedAdventure.getModeration().name(),
                savedAdventure.getGameMode().name(),
                savedAdventure.getOwnerId(),
                savedAdventure.getContextAttributes().nudge(),
                savedAdventure.getContextAttributes().remember(),
                savedAdventure.getContextAttributes().authorsNote(),
                savedAdventure.getContextAttributes().bump(),
                savedAdventure.getContextAttributes().bumpFrequency(),
                savedAdventure.getModelConfiguration().maxTokenLimit(),
                savedAdventure.getModelConfiguration().temperature(),
                savedAdventure.getModelConfiguration().frequencyPenalty(),
                savedAdventure.getModelConfiguration().presencePenalty(),
                savedAdventure.isMultiplayer(),
                savedAdventure.getCreationDate(),
                savedAdventure.getLastUpdateDate(),
                savedAdventure.getModelConfiguration().logitBias(),
                savedAdventure.getModelConfiguration().stopSequences(),
                savedAdventure.getUsersAllowedToRead(),
                savedAdventure.getUsersAllowedToWrite());
    }
}
