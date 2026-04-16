package me.moirai.storyengine.core.port.inbound;

import me.moirai.storyengine.common.cqs.command.Command;

public record GenerateImage(String prompt) implements Command<byte[]> {
}
