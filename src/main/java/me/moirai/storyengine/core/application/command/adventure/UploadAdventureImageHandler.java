package me.moirai.storyengine.core.application.command.adventure;

import com.fasterxml.uuid.Generators;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.ImageResult;
import me.moirai.storyengine.core.port.inbound.adventure.UploadAdventureImage;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;

@CommandHandler
public class UploadAdventureImageHandler extends AbstractCommandHandler<UploadAdventureImage, ImageResult> {

    private final AdventureRepository repository;
    private final StoragePort storagePort;

    public UploadAdventureImageHandler(AdventureRepository repository, StoragePort storagePort) {

        this.repository = repository;
        this.storagePort = storagePort;
    }

    @Override
    public ImageResult execute(UploadAdventureImage command) {

        var adventure = repository.findByPublicId(command.adventureId())
                .orElseThrow(() -> new NotFoundException("Adventure not found"));

        if (adventure.getImageKey() != null) {
            storagePort.delete(adventure.getImageKey());
        }

        var imageId = Generators.timeBasedEpochGenerator().generate();
        var key = "adventures/" + command.adventureId() + "/" + imageId + "." + command.fileExtension();
        storagePort.upload(key, command.imageBytes(), command.contentType());
        adventure.updateImageKey(key);
        repository.save(adventure);

        return new ImageResult(storagePort.resolveUrl(key));
    }
}
