package me.moirai.storyengine.core.application.command.adventure;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.util.stream.Collectors;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.inbound.adventure.ContextAttributesDto;
import me.moirai.storyengine.core.port.inbound.adventure.ModelConfigurationDto;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
public class UpdateAdventureHandler extends AbstractCommandHandler<UpdateAdventure, AdventureDetails> {

    private static final String ADVENTURE_NOT_FOUND = "Adventure to be updated was not found";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "Adventure ID cannot be null or empty";

    private final AdventureRepository repository;
    private final UserRepository userRepository;

    public UpdateAdventureHandler(
            AdventureRepository repository,
            UserRepository userRepository) {

        this.repository = repository;
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
                .orElseThrow(() -> new NotFoundException(ADVENTURE_NOT_FOUND));

        adventure.updateName(command.name());
        adventure.updateNarrator(command.narratorName(), command.narratorPersonality());
        adventure.updateAiModel(command.modelConfiguration().aiModel());
        adventure.updateModeration(command.moderation());
        adventure.updateTemperature(command.modelConfiguration().temperature());
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

        updatePermissions(command, adventure);

        var savedAdventure = repository.save(adventure);
        return mapResult(savedAdventure);
    }

    private void updatePermissions(UpdateAdventure command, Adventure adventure) {

        adventure.updateVisibility(command.visibility());
        var newPermissions = emptyIfNull(command.permissions()).stream()
                .map(dto -> {
                    var user = userRepository.findByPublicId(dto.userId())
                            .orElseThrow(() -> new NotFoundException("User not found"));

                    return new Permission(user.getId(), dto.level());
                })
                .collect(Collectors.toSet());

        adventure.updatePermissions(newPermissions);
    }

    private AdventureDetails mapResult(Adventure savedAdventure) {

        var modelConfiguration = new ModelConfigurationDto(
                savedAdventure.getModelConfiguration().getAiModel(),
                savedAdventure.getModelConfiguration().getMaxTokenLimit(),
                savedAdventure.getModelConfiguration().getTemperature());

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
                savedAdventure.getWorldId(),
                savedAdventure.getNarratorName(),
                savedAdventure.getNarratorPersonality(),
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
                                    .orElseThrow(() -> new NotFoundException("User not found"));

                            return new PermissionDto(user.getPublicId(), permission.level());
                        })
                        .collect(Collectors.toSet()),
                savedAdventure.getLorebook().stream()
                        .map(entry -> new AdventureLorebookEntryDetails(
                                entry.getPublicId(),
                                savedAdventure.getPublicId(),
                                entry.getName(),
                                entry.getDescription(),
                                entry.getPlayerId(),
                                entry.isPlayerCharacter(),
                                entry.getCreationDate(),
                                entry.getLastUpdateDate()))
                        .collect(Collectors.toSet()));
    }
}
