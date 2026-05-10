package me.moirai.storyengine.core.port.inbound.character;

import java.util.UUID;

import me.moirai.storyengine.common.enums.CharacterClass;

public record PlayerCharacterSummary(
        UUID id,
        String ownerUsername,
        String name,
        CharacterClass characterClass,
        String imageUrl) {
}