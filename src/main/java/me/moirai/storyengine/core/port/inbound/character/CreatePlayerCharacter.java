package me.moirai.storyengine.core.port.inbound.character;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.common.enums.CharacterClass;

public record CreatePlayerCharacter(
        String name,
        CharacterClass characterClass,
        String personality,
        String physicalDescription,
        Long requesterId)
        implements Command<PlayerCharacterDetails> {
}