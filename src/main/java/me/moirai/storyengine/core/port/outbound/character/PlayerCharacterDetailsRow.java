package me.moirai.storyengine.core.port.outbound.character;

import java.time.Instant;
import java.util.UUID;

import me.moirai.storyengine.common.enums.CharacterClass;

public record PlayerCharacterDetailsRow(
        UUID id,
        String ownerUsername,
        String name,
        CharacterClass characterClass,
        String personality,
        String physicalDescription,
        String imageKey,
        Instant creationDate,
        Instant lastUpdateDate) {
}