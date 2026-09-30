package me.moirai.storyengine.core.port.inbound.character;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import me.moirai.storyengine.common.enums.CharacterAttribute;
import me.moirai.storyengine.common.enums.CharacterClass;
import me.moirai.storyengine.common.enums.CharacterSkill;
import me.moirai.storyengine.common.enums.SignatureSkill;
import me.moirai.storyengine.common.util.Functions;

public record PlayerCharacterDetails(
        UUID id,
        String ownerUsername,
        String ownerDisplayName,
        String name,
        CharacterClass characterClass,
        String personality,
        String physicalDescription,
        String background,
        Map<CharacterAttribute, Integer> attributes,
        Map<CharacterSkill, Integer> skills,
        Map<SignatureSkill, Integer> signatureSkill,
        int xp,
        int level,
        int unspentAttributePoints,
        int unspentSkillPoints,
        int levelUpXpTarget,
        String imageUrl,
        Double uiImagePositionX,
        Double uiImagePositionY,
        Instant creationDate,
        Instant lastUpdateDate,
        boolean canManage,
        boolean isOwner) {

    public PlayerCharacterDetails {

        attributes = Functions.mapOrDefault(attributes, Map.of(), Map::copyOf);
        skills = Functions.mapOrDefault(skills, Map.of(), Map::copyOf);
        signatureSkill = Functions.mapOrDefault(signatureSkill, Map.of(), Map::copyOf);
    }
}
