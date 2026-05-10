package me.moirai.storyengine.core.port.outbound.character;

import java.util.UUID;

import me.moirai.storyengine.common.enums.CharacterClass;

public record PlayerCharacterSummaryRow(
        UUID id,
        String ownerUsername,
        String name,
        CharacterClass characterClass,
        String imageKey) {
}