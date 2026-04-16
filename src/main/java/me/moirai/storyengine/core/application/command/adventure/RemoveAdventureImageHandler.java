package me.moirai.storyengine.core.application.command.adventure;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.adventure.RemoveAdventureImage;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;

@CommandHandler
public class RemoveAdventureImageHandler extends AbstractCommandHandler<RemoveAdventureImage, Void> {

    private final AdventureRepository repository;
    private final StoragePort storagePort;

    public RemoveAdventureImageHandler(AdventureRepository repository, StoragePort storagePort) {

        this.repository = repository;
        this.storagePort = storagePort;
    }

    @Override
    public void validate(RemoveAdventureImage command) {

        if (command.adventureId() == null) {
            throw new IllegalArgumentException("Adventure ID cannot be null");
        }
    }

    @Override
    public Void execute(RemoveAdventureImage command) {

        var adventure = repository.findByPublicId(command.adventureId())
                .orElseThrow(() -> new NotFoundException("Adventure not found"));

        if (adventure.getImageKey() != null) {
            storagePort.delete(adventure.getImageKey());
            adventure.updateImageKey(null);
            repository.save(adventure);
        }

        return null;
    }
}
