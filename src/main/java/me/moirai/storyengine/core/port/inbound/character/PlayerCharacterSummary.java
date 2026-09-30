package me.moirai.storyengine.core.port.inbound.character;

import java.util.UUID;

import me.moirai.storyengine.common.enums.CharacterClass;

public record PlayerCharacterSummary(
        UUID id,
        String ownerUsername,
        String ownerDisplayName,
        String name,
        CharacterClass characterClass,
        String background,
        String imageUrl,
        Double uiImagePositionX,
        Double uiImagePositionY) {
}