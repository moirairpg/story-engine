package me.moirai.storyengine.core.application.command.adventure;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.collections4.MapUtils.emptyIfNull;

import java.util.UUID;
import java.util.stream.Collectors;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.inbound.adventure.ContextAttributesDto;
import me.moirai.storyengine.core.port.inbound.adventure.ModelConfigurationDto;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@CommandHandler
public class UpdateAdventureHandler extends AbstractCommandHandler<UpdateAdventure, AdventureDetails> {

    private static final String ADVENTURE_NOT_FOUND = "Adventure to be updated was not found";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "Adventure ID cannot be null or empty";
    private static final String PERSONA_NOT_FOUND = "Persona not found";
    private static final String WORLD_NOT_FOUND = "World not found";

    private final AdventureRepository repository;
    private final PersonaRepository personaRepository;
    private final WorldRepository worldRepository;
    private final UserRepository userRepository;

    public UpdateAdventureHandler(
            AdventureRepository repository,
            PersonaRepository personaRepository,
            WorldRepository worldRepository,
            UserRepository userRepository) {

        this.repository = repository;
        this.personaRepository = personaRepository;
        this.worldRepository = worldRepository;
        this.userRepository = userRepository;
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

        var persona = personaRepository.findByPublicId(command.personaId())
                .orElseThrow(() -> new AssetNotFoundException(PERSONA_NOT_FOUND));

        var world = worldRepository.findByPublicId(command.worldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_NOT_FOUND));

        adventure.updateName(command.name());
        adventure.updateWorld(world.getId());
        adventure.updatePersona(persona.getId());
        adventure.updateAiModel(command.modelConfiguration().aiModel());
        adventure.updateModeration(command.moderation());
        adventure.updateTemperature(command.modelConfiguration().temperature());
        adventure.updateFrequencyPenalty(command.modelConfiguration().frequencyPenalty());
        adventure.updatePresencePenalty(command.modelConfiguration().presencePenalty());
        adventure.updateAdventureStart(command.adventureStart());
        adventure.updateDescription(command.description());
        adventure.updateNudge(command.contextAttributes().nudge());
        adventure.updateScene(command.contextAttributes().scene());
        adventure.updateAuthorsNote(command.contextAttributes().authorsNote());
        adventure.updateBump(command.contextAttributes().bump());
        adventure.updateBumpFrequency(command.contextAttributes().bumpFrequency());

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

        var newPermissions = emptyIfNull(command.permissions()).stream()
                .map(dto -> {
                    var user = userRepository.findByPublicId(dto.userId())
                            .orElseThrow(() -> new AssetNotFoundException("User not found"));

                    return new Permission(user.getId(), dto.level());
                })
                .collect(Collectors.toSet());

        adventure.updatePermissions(newPermissions);
    }

    private void updateLogitBias(UpdateAdventure command, Adventure adventure) {

        emptyIfNull(command.modelConfiguration().logitBiasToAdd())
                .entrySet()
                .stream()
                .forEach(entry -> adventure.addLogitBias(entry.getKey(), entry.getValue()));

        emptyIfNull(command.modelConfiguration().logitBiasToRemove())
                .forEach(adventure::removeLogitBias);
    }

    private void updateStopSequences(UpdateAdventure command, Adventure adventure) {

        emptyIfNull(command.modelConfiguration().stopSequencesToAdd())
                .stream()
                .forEach(adventure::addStopSequence);

        emptyIfNull(command.modelConfiguration().stopSequencesToRemove())
                .forEach(adventure::removeStopSequence);
    }

    private AdventureDetails mapResult(Adventure savedAdventure, UUID personaPublicId, UUID worldPublicId) {

        var modelConfiguration = new ModelConfigurationDto(
                savedAdventure.getModelConfiguration().getAiModel(),
                savedAdventure.getModelConfiguration().getMaxTokenLimit(),
                savedAdventure.getModelConfiguration().getTemperature(),
                savedAdventure.getModelConfiguration().getFrequencyPenalty(),
                savedAdventure.getModelConfiguration().getPresencePenalty(),
                savedAdventure.getModelConfiguration().getStopSequences(),
                savedAdventure.getModelConfiguration().getLogitBias());

        var contextAttributes = new ContextAttributesDto(
                savedAdventure.getContextAttributes().nudge(),
                savedAdventure.getContextAttributes().authorsNote(),
                savedAdventure.getContextAttributes().scene(),
                savedAdventure.getContextAttributes().bump(),
                savedAdventure.getContextAttributes().bumpFrequency());

        return new AdventureDetails(
                savedAdventure.getPublicId(),
                savedAdventure.getName(),
                savedAdventure.getDescription(),
                savedAdventure.getAdventureStart(),
                worldPublicId,
                personaPublicId,
                savedAdventure.getVisibility(),
                savedAdventure.getModeration(),
                savedAdventure.isMultiplayer(),
                savedAdventure.getCreationDate(),
                savedAdventure.getLastUpdateDate(),
                modelConfiguration,
                contextAttributes,
                savedAdventure.getPermissions().stream()
                        .map(permission -> {
                            var user = userRepository.findById(permission.userId())
                                    .orElseThrow(() -> new AssetNotFoundException("User not found"));

                            return new PermissionDto(user.getPublicId(), permission.level());
                        })
                        .collect(Collectors.toSet()),
                savedAdventure.getLorebook().stream()
                        .map(entry -> new AdventureLorebookEntryDetails(
                                entry.getPublicId(),
                                savedAdventure.getPublicId(),
                                entry.getName(),
                                entry.getRegex(),
                                entry.getDescription(),
                                entry.getPlayerId(),
                                entry.isPlayerCharacter(),
                                entry.getCreationDate(),
                                entry.getLastUpdateDate()))
                        .collect(Collectors.toSet()));
    }
}
