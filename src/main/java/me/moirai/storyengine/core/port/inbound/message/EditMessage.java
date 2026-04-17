package me.moirai.storyengine.core.port.inbound.message;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;

public record EditMessage(UUID adventureId, UUID messageId, String content, String username) implements Command<Void> {
}
