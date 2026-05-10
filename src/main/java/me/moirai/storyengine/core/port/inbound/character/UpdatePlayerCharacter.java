package me.moirai.storyengine.core.port.inbound.character;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.common.enums.CharacterClass;

public record UpdatePlayerCharacter(
        UUID characterId,
        String name,
        CharacterClass characterClass,
        String personality,
        String physicalDescription)
        implements Command<PlayerCharacterDetails> {
}
