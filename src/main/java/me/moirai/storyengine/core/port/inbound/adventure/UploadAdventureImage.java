package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.core.port.inbound.ImageResult;

public record UploadAdventureImage(
        UUID adventureId,
        byte[] imageBytes,
        String contentType,
        String fileExtension)
        implements Command<ImageResult> {
}
