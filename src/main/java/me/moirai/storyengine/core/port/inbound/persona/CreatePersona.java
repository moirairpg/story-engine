package me.moirai.storyengine.core.port.inbound.persona;

import java.util.Set;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.common.enums.Visibility;

public record CreatePersona(
        String name,
        String personality,
        Visibility visibility,
        String requesterId,
        Set<String> usersAllowedToWrite,
        Set<String> usersAllowedToRead) implements Command<PersonaDetails> {
}
