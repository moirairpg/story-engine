package me.moirai.storyengine.core.port.inbound.world;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.core.port.inbound.ImageResult;

public record UploadWorldImage(
        UUID worldId,
        byte[] imageBytes,
        String contentType,
        String fileExtension)
        implements Command<ImageResult> {
}
