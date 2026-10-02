package me.moirai.storyengine.core.application.command.world;

import static me.moirai.storyengine.common.enums.PermissionLevel.OWNER;

import java.util.stream.Collectors;

import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorld;
import me.moirai.storyengine.core.port.inbound.world.WorldDetails;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@CommandHandler
@Authorize(operation = AuthorizationOperation.UPDATE_WORLD, fields = "#request.worldId")
public class UpdateWorldHandler extends AbstractCommandHandler<UpdateWorld, WorldDetails> {

    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "World ID cannot be null or empty";
    private static final String WORLD_NOT_FOUND = "World to be updated was not found";
    private static final String REQUESTER_NOT_FOUND = "Requester of the world update was not found";

    private final WorldRepository repository;
    private final UserRepository userRepository;
    private final StoragePort storagePort;

    public UpdateWorldHandler(
            WorldRepository repository,
            UserRepository userRepository,
            StoragePort storagePort) {

        this.repository = repository;
        this.userRepository = userRepository;
        this.storagePort = storagePort;
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
        world.updateUiImagePosition(command.uiImagePositionX(), command.uiImagePositionY());

        command.lorebookEntriesToDelete()
                .forEach(world::removeLorebookEntry);

        command.lorebookEntriesToUpdate()
                .forEach(e -> world.updateLorebookEntry(e.id(), e.name(), e.description()));

        command.lorebookEntriesToAdd()
                .forEach(e -> world.addLorebookEntry(e.name(), e.description()));

        var saved = repository.save(world);

        var requester = userRepository.findByPublicId(command.requesterId())
                .orElseThrow(() -> new NotFoundException(REQUESTER_NOT_FOUND));

        var isOwner = saved.getPermissions().stream()
                .anyMatch(permission -> permission.level() == OWNER
                        && permission.userId().equals(requester.getId()));

        return mapResult(saved, isOwner);
    }

    private WorldDetails mapResult(World world, boolean isOwner) {

        return new WorldDetails(
                world.getPublicId(),
                world.getName(),
                world.getDescription(),
                world.getAdventureStart(),
                world.getNarratorName(),
                world.getNarratorPersonality(),
                world.getVisibility().name(),
                storagePort.resolveUrl(world.getImageKey()),
                true,
                isOwner,
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
                world.getLastUpdateDate(),
                world.getUiImagePositionX(),
                world.getUiImagePositionY());
    }
}
