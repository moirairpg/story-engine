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
        Set<Long> usersAllowedToWriteToAdd,
        Set<Long> usersAllowedToWriteToRemove,
        Set<Long> usersAllowedToReadToAdd,
        Set<Long> usersAllowedToReadToRemove)
        implements Command<PersonaDetails> {
}
