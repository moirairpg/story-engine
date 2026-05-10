package me.moirai.storyengine.core.port.inbound.character;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.core.port.inbound.ImageResult;

public record UploadPlayerCharacterImage(
        UUID characterId,
        byte[] bytes,
        String contentType,
        String extension)
        implements Command<ImageResult> {
}