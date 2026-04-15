package me.moirai.storyengine.core.application.command.world;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.util.stream.Collectors;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorld;
import me.moirai.storyengine.core.port.inbound.world.WorldDetails;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@CommandHandler
public class UpdateWorldHandler extends AbstractCommandHandler<UpdateWorld, WorldDetails> {

    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "World ID cannot be null or empty";
    private static final String WORLD_NOT_FOUND = "World to be updated was not found";

    private final WorldRepository repository;
    private final UserRepository userRepository;

    public UpdateWorldHandler(
            WorldRepository repository,
            UserRepository userRepository) {

        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Override
    public void validate(UpdateWorld command) {

        if (command.worldId() == null) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public WorldDetails execute(UpdateWorld command) {

        var world = repository.findByPublicId(command.worldId())
                .orElseThrow(() -> new NotFoundException(WORLD_NOT_FOUND));

        world.updateName(command.name());
        world.updateDescription(command.description());
        world.updateAdventureStart(command.adventureStart());
        world.updateNarrator(command.narratorName(), command.narratorPersonality());
        world.updateVisibility(command.visibility());

        var newPermissions = emptyIfNull(command.permissions()).stream()
                .map(dto -> {
                    var user = userRepository.findByPublicId(dto.userId())
                            .orElseThrow(() -> new NotFoundException("User not found"));

                    return new Permission(user.getId(), dto.level());
                })
                .collect(Collectors.toSet());

        world.updatePermissions(newPermissions);

        command.lorebookEntriesToDelete()
                .forEach(world::removeLorebookEntry);

        command.lorebookEntriesToUpdate()
                .forEach(e -> world.updateLorebookEntry(e.id(), e.name(), e.description()));

        command.lorebookEntriesToAdd()
                .forEach(e -> world.addLorebookEntry(e.name(), e.description()));

        return mapResult(repository.save(world));
    }

    private WorldDetails mapResult(World world) {

        return new WorldDetails(
                world.getPublicId(),
                world.getName(),
                world.getDescription(),
                world.getAdventureStart(),
                world.getNarratorName(),
                world.getNarratorPersonality(),
                world.getVisibility().name(),
                world.getPermissions().stream()
                        .map(permission -> {
                            var user = userRepository.findById(permission.userId())
                                    .orElseThrow(() -> new NotFoundException("User not found"));

                            return new PermissionDto(user.getPublicId(), permission.level());
                        })
                        .collect(Collectors.toSet()),
                world.getLorebook().stream()
                        .map(entry -> new WorldLorebookEntryDetails(
                                entry.getPublicId(),
                                world.getPublicId(),
                                entry.getName(),
                                entry.getDescription(),
                                entry.getCreationDate(),
                                entry.getLastUpdateDate()))
                        .collect(Collectors.toSet()),
                world.getCreationDate(),
                world.getLastUpdateDate());
    }
}
