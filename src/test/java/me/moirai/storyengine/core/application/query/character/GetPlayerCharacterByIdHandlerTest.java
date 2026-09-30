package me.moirai.storyengine.core.application.query.character;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.enums.CharacterAttribute;
import me.moirai.storyengine.common.enums.CharacterClass;
import me.moirai.storyengine.common.enums.CharacterSkill;
import me.moirai.storyengine.common.enums.SignatureSkill;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.character.PlayerCharacterFixture;
import me.moirai.storyengine.core.port.inbound.character.GetPlayerCharacterById;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterDetailsRow;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterReader;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;

@ExtendWith(MockitoExtension.class)
public class GetPlayerCharacterByIdHandlerTest {

    private static final String OWNER_USERNAME = "john.doe";
    private static final String OWNER_DISPLAY_NAME = "Johnny the Bold";
    private static final String OTHER_USERNAME = "jane.doe";

    @Mock
    private PlayerCharacterReader reader;

    @Mock
    private StoragePort storagePort;

    @InjectMocks
    private GetPlayerCharacterByIdHandler handler;

    @Test
    public void shouldThrowExceptionWhenCharacterIsNotFound() {

        // given
        var query = new GetPlayerCharacterById(PlayerCharacterFixture.PUBLIC_ID, OWNER_USERNAME);

        when(reader.getById(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> handler.handle(query));
    }

    @Test
    public void shouldReturnBothFlagsAsTrueWhenRequesterIsTheOwner() {

        // given
        var query = new GetPlayerCharacterById(PlayerCharacterFixture.PUBLIC_ID, OWNER_USERNAME);

        when(reader.getById(any(UUID.class))).thenReturn(Optional.of(characterRow()));

        // when
        var result = handler.handle(query);

        // then
        assertThat(result.canManage()).isTrue();
        assertThat(result.isOwner()).isTrue();
        assertThat(result.background()).isEqualTo("Orphaned.");
        assertThat(result.ownerUsername()).isEqualTo(OWNER_USERNAME);
        assertThat(result.ownerDisplayName()).isEqualTo(OWNER_DISPLAY_NAME);
    }

    @Test
    public void shouldReturnBothFlagsAsFalseWhenRequesterIsNotTheOwner() {

        // given
        var query = new GetPlayerCharacterById(PlayerCharacterFixture.PUBLIC_ID, OTHER_USERNAME);

        when(reader.getById(any(UUID.class))).thenReturn(Optional.of(characterRow()));

        // when
        var result = handler.handle(query);

        // then
        assertThat(result.canManage()).isFalse();
        assertThat(result.isOwner()).isFalse();
    }

    @Test
    public void shouldReturnTheSkillAndSignatureLevelsWhenTheCharacterExists() {

        // given
        var query = new GetPlayerCharacterById(PlayerCharacterFixture.PUBLIC_ID, OWNER_USERNAME);

        when(reader.getById(any(UUID.class))).thenReturn(Optional.of(characterRow()));

        // when
        var result = handler.handle(query);

        // then
        assertThat(result.skills())
                .containsEntry(CharacterSkill.PERSUASION, 2)
                .containsEntry(CharacterSkill.ENDURANCE, 2);
        assertThat(result.signatureSkill()).containsEntry(SignatureSkill.ZEAL, 1);
    }

    @Test
    public void shouldReturnTheAttributeLevelsWhenTheCharacterExists() {

        // given
        var query = new GetPlayerCharacterById(PlayerCharacterFixture.PUBLIC_ID, OWNER_USERNAME);

        when(reader.getById(any(UUID.class))).thenReturn(Optional.of(characterRow()));

        // when
        var result = handler.handle(query);

        // then
        assertThat(result.attributes())
                .containsEntry(CharacterAttribute.STRENGTH, 3)
                .containsEntry(CharacterAttribute.VIGOR, 2)
                .containsEntry(CharacterAttribute.CHARISMA, 1);
    }

    @Test
    public void shouldReturnAnEmptySignatureMapWhenTheCharacterHasNoClass() {

        // given
        var query = new GetPlayerCharacterById(PlayerCharacterFixture.PUBLIC_ID, OWNER_USERNAME);
        var skills = new EnumMap<CharacterSkill, Integer>(CharacterSkill.class);
        Arrays.stream(CharacterSkill.values()).forEach(skill -> skills.put(skill, 0));

        var row = new PlayerCharacterDetailsRow(
                PlayerCharacterFixture.PUBLIC_ID,
                OWNER_USERNAME,
                OWNER_DISPLAY_NAME,
                "Volin Habar",
                null,
                "Brave.",
                "Tall.",
                "Orphaned.",
                PlayerCharacterFixture.sampleAttributeAllocation(),
                skills,
                0,
                0,
                1,
                0,
                0,
                null,
                null,
                null,
                null,
                null);

        when(reader.getById(any(UUID.class))).thenReturn(Optional.of(row));

        // when
        var result = handler.handle(query);

        // then
        assertThat(result.characterClass()).isNull();
        assertThat(result.signatureSkill()).isEmpty();
    }

    private PlayerCharacterDetailsRow characterRow() {

        return new PlayerCharacterDetailsRow(
                PlayerCharacterFixture.PUBLIC_ID,
                OWNER_USERNAME,
                OWNER_DISPLAY_NAME,
                "Volin Habar",
                CharacterClass.PALADIN,
                "Brave.",
                "Tall.",
                "Orphaned.",
                PlayerCharacterFixture.sampleAttributeAllocation(),
                PlayerCharacterFixture.sampleSkillAllocation(),
                1,
                0,
                1,
                0,
                0,
                null,
                null,
                null,
                null,
                null);
    }
}
