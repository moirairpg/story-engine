package me.moirai.storyengine.core.port.inbound.persona;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;

public record DeletePersona(
        UUID personaId) implements Command<Void> {
}
