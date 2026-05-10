package me.moirai.storyengine.core.port.inbound.character;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;

public record DeletePlayerCharacter(UUID characterId) implements Command<Void> {
}
