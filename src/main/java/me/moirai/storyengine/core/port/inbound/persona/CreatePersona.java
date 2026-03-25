package me.moirai.storyengine.core.port.inbound.persona;

import java.util.Set;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.common.enums.Visibility;

public record CreatePersona(
        String name,
        String personality,
        Visibility visibility,
        Set<Long> usersAllowedToWrite,
        Set<Long> usersAllowedToRead) implements Command<PersonaDetails> {
}
