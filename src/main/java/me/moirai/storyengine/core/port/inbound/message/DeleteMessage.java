package me.moirai.storyengine.core.port.inbound.message;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;

public record DeleteMessage(UUID adventureId, UUID messageId) implements Command<Void> {
}
