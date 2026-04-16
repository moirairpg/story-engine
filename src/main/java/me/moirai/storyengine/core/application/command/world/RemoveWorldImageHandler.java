package me.moirai.storyengine.core.application.command.world;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.world.RemoveWorldImage;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@CommandHandler
public class RemoveWorldImageHandler extends AbstractCommandHandler<RemoveWorldImage, Void> {

    private final WorldRepository repository;
    private final StoragePort storagePort;

    public RemoveWorldImageHandler(WorldRepository repository, StoragePort storagePort) {

        this.repository = repository;
        this.storagePort = storagePort;
    }

    @Override
    public void validate(RemoveWorldImage command) {

        if (command.worldId() == null) {
            throw new IllegalArgumentException("World ID cannot be null");
        }
    }

    @Override
    public Void execute(RemoveWorldImage command) {

        var world = repository.findByPublicId(command.worldId())
                .orElseThrow(() -> new NotFoundException("World not found"));

        if (world.getImageKey() != null) {
            storagePort.delete(world.getImageKey());
            world.updateImageKey(null);
            repository.save(world);
        }

        return null;
    }
}
