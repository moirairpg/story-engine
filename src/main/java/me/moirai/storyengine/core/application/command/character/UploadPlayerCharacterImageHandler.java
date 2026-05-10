package me.moirai.storyengine.core.application.command.character;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.ImageResult;
import me.moirai.storyengine.core.port.inbound.character.UploadPlayerCharacterImage;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterRepository;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;

@CommandHandler
public class UploadPlayerCharacterImageHandler extends AbstractCommandHandler<UploadPlayerCharacterImage, ImageResult> {

    private final PlayerCharacterRepository repository;
    private final StoragePort storagePort;

    public UploadPlayerCharacterImageHandler(
            PlayerCharacterRepository repository,
            StoragePort storagePort) {

        this.repository = repository;
        this.storagePort = storagePort;
    }

    @Override
    public ImageResult execute(UploadPlayerCharacterImage command) {

        var character = repository.findByPublicId(command.characterId())
                .orElseThrow(() -> new NotFoundException("Character not found"));

        if (character.getImageKey() != null) {
            storagePort.delete(character.getImageKey());
        }

        var key = character.generateImageKey();
        storagePort.upload(key, command.bytes(), command.contentType());
        character.updateImageKey(key);
        repository.save(character);

        return new ImageResult(storagePort.resolveUrl(key));
    }
}