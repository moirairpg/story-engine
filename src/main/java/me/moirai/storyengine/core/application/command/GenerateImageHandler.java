package me.moirai.storyengine.core.application.command;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.core.port.inbound.GenerateImage;
import me.moirai.storyengine.core.port.outbound.ai.ImageGenerationPort;

@CommandHandler
public class GenerateImageHandler extends AbstractCommandHandler<GenerateImage, byte[]> {

    private final ImageGenerationPort imageGenerationPort;

    public GenerateImageHandler(ImageGenerationPort imageGenerationPort) {
        this.imageGenerationPort = imageGenerationPort;
    }

    @Override
    public byte[] execute(GenerateImage command) {
        return imageGenerationPort.generate(command.prompt());
    }
}
