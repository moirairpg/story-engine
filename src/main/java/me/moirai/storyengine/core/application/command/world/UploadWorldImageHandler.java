package me.moirai.storyengine.core.application.command.world;

import com.fasterxml.uuid.Generators;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.ImageResult;
import me.moirai.storyengine.core.port.inbound.world.UploadWorldImage;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@CommandHandler
public class UploadWorldImageHandler extends AbstractCommandHandler<UploadWorldImage, ImageResult> {

    private final WorldRepository repository;
    private final StoragePort storagePort;

    public UploadWorldImageHandler(WorldRepository repository, StoragePort storagePort) {

        this.repository = repository;
        this.storagePort = storagePort;
    }

    @Override
    public ImageResult execute(UploadWorldImage command) {

        var world = repository.findByPublicId(command.worldId())
                .orElseThrow(() -> new NotFoundException("World not found"));

        if (world.getImageKey() != null) {
            storagePort.delete(world.getImageKey());
        }

        var imageId = Generators.timeBasedEpochGenerator().generate();
        var key = "worlds/" + command.worldId() + "/" + imageId + "." + command.fileExtension();
        storagePort.upload(key, command.imageBytes(), command.contentType());
        world.updateImageKey(key);
        repository.save(world);

        return new ImageResult(storagePort.resolveUrl(key));
    }
}
