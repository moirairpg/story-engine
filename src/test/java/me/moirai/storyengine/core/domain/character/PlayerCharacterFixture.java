package me.moirai.storyengine.core.domain.character;

import java.util.UUID;

import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.enums.CharacterClass;

public class PlayerCharacterFixture {

    public static final UUID PUBLIC_ID = UUID.fromString("857345aa-4444-0000-0000-000000000000");
    public static final Long NUMERIC_ID = 4L;
    public static final Long PLAYER_ID = 1111L;

    public static PlayerCharacter.Builder samplePlayerCharacter() {

        var builder = PlayerCharacter.builder();
        builder.name("Volin Habar");
        builder.playerId(PLAYER_ID);
        builder.personality("Brave, honorable and disciplined.");
        builder.physicalDescription("A tall warrior with long black hair and a scar across his left cheek.");
        builder.characterClass(CharacterClass.PALADIN);

        return builder;
    }

    public static PlayerCharacter samplePlayerCharacterWithId() {

        var character = samplePlayerCharacter().build();
        ReflectionTestUtils.setField(character, "id", NUMERIC_ID);
        ReflectionTestUtils.setField(character, "publicId", PUBLIC_ID);

        return character;
    }
}
