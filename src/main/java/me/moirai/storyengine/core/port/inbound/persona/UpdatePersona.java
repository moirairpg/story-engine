package me.moirai.storyengine.core.port.inbound.persona;

import java.util.Set;
import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.common.enums.Visibility;

public record UpdatePersona(
        UUID personaId,
        String name,
        String personality,
        Visibility visibility,
        String requesterId,
        Set<String> usersAllowedToWriteToAdd,
        Set<String> usersAllowedToWriteToRemove,
        Set<String> usersAllowedToReadToAdd,
        Set<String> usersAllowedToReadToRemove)
        implements Command<PersonaDetails> {
}