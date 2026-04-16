package me.moirai.storyengine.core.application.command.world;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.world.DeleteWorld;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@CommandHandler
public class DeleteWorldHandler extends AbstractCommandHandler<DeleteWorld, Void> {

    private static final String WORLD_TO_BE_VIEWED_WAS_NOT_FOUND = "World to be viewed was not found";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "World ID cannot be null or empty";

    private final WorldRepository repository;
    private final StoragePort storagePort;

    public DeleteWorldHandler(WorldRepository repository, StoragePort storagePort) {
        this.repository = repository;
        this.storagePort = storagePort;
    }

    @Override
    public void validate(DeleteWorld command) {

        if (command.worldId() == null) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public Void execute(DeleteWorld command) {

        var world = repository.findByPublicId(command.worldId())
                .orElseThrow(() -> new NotFoundException(WORLD_TO_BE_VIEWED_WAS_NOT_FOUND));

        if (world.getImageKey() != null) {
            storagePort.delete(world.getImageKey());
        }

        repository.deleteByPublicId(command.worldId());

        return null;
    }
}
