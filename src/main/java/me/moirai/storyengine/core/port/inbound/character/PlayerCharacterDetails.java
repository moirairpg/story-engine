package me.moirai.storyengine.core.port.inbound.character;

import java.time.Instant;
import java.util.UUID;

import me.moirai.storyengine.common.enums.CharacterClass;

public record PlayerCharacterDetails(
        UUID id,
        String ownerUsername,
        String name,
        CharacterClass characterClass,
        String personality,
        String physicalDescription,
        String imageUrl,
        Instant creationDate,
        Instant lastUpdateDate) {
}