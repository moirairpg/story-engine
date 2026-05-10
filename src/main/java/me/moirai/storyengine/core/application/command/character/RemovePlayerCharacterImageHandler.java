package me.moirai.storyengine.core.application.command.character;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.character.RemovePlayerCharacterImage;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterRepository;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;

@CommandHandler
public class RemovePlayerCharacterImageHandler extends AbstractCommandHandler<RemovePlayerCharacterImage, Void> {

    private final PlayerCharacterRepository repository;
    private final StoragePort storagePort;

    public RemovePlayerCharacterImageHandler(
            PlayerCharacterRepository repository,
            StoragePort storagePort) {

        this.repository = repository;
        this.storagePort = storagePort;
    }

    @Override
    public void validate(RemovePlayerCharacterImage command) {

        if (command.characterId() == null) {
            throw new IllegalArgumentException("Character ID cannot be null");
        }
    }

    @Override
    public Void execute(RemovePlayerCharacterImage command) {

        var character = repository.findByPublicId(command.characterId())
                .orElseThrow(() -> new NotFoundException("Character not found"));

        if (character.getImageKey() != null) {
            storagePort.delete(character.getImageKey());
            character.updateImageKey(null);
            repository.save(character);
        }

        return null;
    }
}
