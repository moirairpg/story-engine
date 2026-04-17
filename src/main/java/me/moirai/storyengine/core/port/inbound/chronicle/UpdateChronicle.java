package me.moirai.storyengine.core.port.inbound.chronicle;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;

public record UpdateChronicle(UUID adventurePublicId) implements Command<Void> {
}
