package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.UUID;

import me.moirai.storyengine.common.enums.CharacterClass;

public record AdventureMembershipSummary(
        UUID playerCharacterId,
        UUID playerId,
        String playerUsername,
        String playerDisplayName,
        String name,
        CharacterClass characterClass,
        String imageUrl,
        Double uiImagePositionX,
        Double uiImagePositionY) {
}
