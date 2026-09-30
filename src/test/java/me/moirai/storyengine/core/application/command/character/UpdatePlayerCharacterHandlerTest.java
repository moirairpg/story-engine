package me.moirai.storyengine.core.application.command.character;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.enums.CharacterAttribute;
import me.moirai.storyengine.common.enums.CharacterClass;
import me.moirai.storyengine.common.enums.CharacterSkill;
import me.moirai.storyengine.common.enums.SignatureSkill;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.character.PlayerCharacter;
import me.moirai.storyengine.core.domain.character.PlayerCharacterFixture;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.inbound.character.UpdatePlayerCharacter;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterRepository;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterVectorSearchPort;
import me.moirai.storyengine.core.port.outbound.generation.EmbeddingPort;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UpdatePlayerCharacterHandlerTest {

    private static final float[] VECTOR = new float[] { 0.1f, 0.2f };
    private static final String OWNER_USERNAME = "john.doe";
    private static final String BACKGROUND = "Orphaned.";

    @Mock
    private PlayerCharacterRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlayerCharacterVectorSearchPort vectorSearchPort;

    @Mock
    private EmbeddingPort embeddingPort;

    @Mock
    private StoragePort storagePort;

    @InjectMocks
    private UpdatePlayerCharacterHandler handler;

    @Test
    void shouldApplyEveryUpdatedFieldWhenTheCharacterIsUpdated() {

        // given
        var character = PlayerCharacterFixture.samplePlayerCharacterWithId();
        var command = updateCommand(character.getPublicId(), "Volin the Bold", "Reckless.", "Short.");

        when(repository.findByPublicId(character.getPublicId())).thenReturn(Optional.of(character));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(character.getPlayerId())).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.execute(command);

        // then
        assertThat(result.background()).isEqualTo(BACKGROUND);
        assertThat(result.ownerUsername()).isEqualTo("john.doe");
        assertThat(result.ownerDisplayName()).isEqualTo("John Doe");
        assertThat(character.getName()).isEqualTo("Volin the Bold");
        assertThat(character.getPersonality()).isEqualTo("Reckless.");
        assertThat(character.getPhysicalDescription()).isEqualTo("Short.");
        assertThat(character.getBackground()).isEqualTo(BACKGROUND);
        assertThat(character.getCharacterClass()).isEqualTo(CharacterClass.PALADIN);
        assertThat(character.getUiImagePositionX()).isEqualTo(0.25);
        assertThat(character.getUiImagePositionY()).isEqualTo(0.75);
    }

    @Test
    void shouldReindexWithTheUpdatedDescriptionWhenTheCharacterIsUpdated() {

        // given
        var character = PlayerCharacterFixture.samplePlayerCharacterWithId();
        var command = updateCommand(character.getPublicId(), "Volin the Bold", "Reckless.", "Short.");

        when(repository.findByPublicId(character.getPublicId())).thenReturn(Optional.of(character));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(embeddingPort.embed(any())).thenReturn(VECTOR);
        when(userRepository.findById(character.getPlayerId())).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        handler.execute(command);

        // then
        var embedded = ArgumentCaptor.forClass(String.class);
        verify(embeddingPort).embed(embedded.capture());

        assertThat(embedded.getValue()).isEqualTo("Volin the Bold: PALADIN; Reckless.; Short.; Orphaned.");

        verify(vectorSearchPort).upsert(character.getPublicId(), VECTOR);
    }

    @Test
    void shouldThrowExceptionWhenTheCharacterIsNotFound() {

        // given
        var characterId = UUID.randomUUID();
        var command = updateCommand(characterId, "Volin", "Brave.", "Tall.");

        when(repository.findByPublicId(characterId)).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> handler.execute(command));

        verify(repository, never()).save(any());
        verify(vectorSearchPort, never()).upsert(any(), any());
    }

    @Test
    void shouldThrowExceptionWhenTheOwnerIsNotFound() {

        // given
        var character = PlayerCharacterFixture.samplePlayerCharacterWithId();
        var command = updateCommand(character.getPublicId(), "Volin", "Brave.", "Tall.");

        when(repository.findByPublicId(character.getPublicId())).thenReturn(Optional.of(character));
        when(userRepository.findById(character.getPlayerId())).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> handler.execute(command));

        verify(repository, never()).save(any());
        verify(vectorSearchPort, never()).upsert(any(), any());
    }

    @Test
    void shouldThrowExceptionWhenTheCharacterHasNoClass() {

        // given
        var character = mock(PlayerCharacter.class);
        var command = updateCommand(UUID.randomUUID(), "Volin", "Brave.", "Tall.");

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(character));
        when(character.getPlayerId()).thenReturn(PlayerCharacterFixture.PLAYER_ID);
        when(userRepository.findById(PlayerCharacterFixture.PLAYER_ID))
                .thenReturn(Optional.of(UserFixture.playerWithId()));
        doThrow(new BusinessRuleViolationException("Character needs a class")).when(character).validateHasClass();

        // then
        assertThrows(BusinessRuleViolationException.class, () -> handler.execute(command));

        verify(repository, never()).save(any());
    }

    @Test
    void shouldReturnTheCommittedAttributeLevelsWhenTheCharacterIsUpdated() {

        // given
        var character = PlayerCharacterFixture.samplePlayerCharacterWithId();
        var command = updateCommand(character.getPublicId(), "Volin", "Brave.", "Tall.");

        when(repository.findByPublicId(character.getPublicId())).thenReturn(Optional.of(character));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(character.getPlayerId())).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.execute(command);

        // then
        assertThat(result.attributes())
                .containsEntry(CharacterAttribute.STRENGTH, 3)
                .containsEntry(CharacterAttribute.VIGOR, 2)
                .containsEntry(CharacterAttribute.CHARISMA, 1);
    }

    @Test
    void shouldReturnTheCommittedClassAndSkillLevelsWhenTheCharacterIsUpdated() {

        // given
        var character = PlayerCharacterFixture.samplePlayerCharacterWithId();
        var command = updateCommand(character.getPublicId(), "Volin", "Brave.", "Tall.");

        when(repository.findByPublicId(character.getPublicId())).thenReturn(Optional.of(character));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(character.getPlayerId())).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.execute(command);

        // then
        assertThat(result.characterClass()).isEqualTo(CharacterClass.PALADIN);
        assertThat(result.skills())
                .containsEntry(CharacterSkill.PERSUASION, 2)
                .containsEntry(CharacterSkill.ENDURANCE, 2);
        assertThat(result.signatureSkill()).containsEntry(SignatureSkill.ZEAL, 1);
    }

    @Test
    void shouldReturnBothFlagsAsTrueWhenTheRequesterOwnsTheCharacter() {

        // given
        var character = PlayerCharacterFixture.samplePlayerCharacterWithId();
        var command = updateCommand(character.getPublicId(), "Volin", "Brave.", "Tall.");

        when(repository.findByPublicId(character.getPublicId())).thenReturn(Optional.of(character));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(character.getPlayerId())).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.execute(command);

        // then
        assertThat(result.canManage()).isTrue();
        assertThat(result.isOwner()).isTrue();
    }

    @Test
    void shouldNotReturnIsOwnerWhenTheRequesterDoesNotOwnTheCharacter() {

        // given
        var character = PlayerCharacterFixture.samplePlayerCharacterWithId();
        var command = new UpdatePlayerCharacter(
                character.getPublicId(), "Volin", "Brave.", "Tall.", BACKGROUND, 0.25, 0.75, "jane.doe");

        when(repository.findByPublicId(character.getPublicId())).thenReturn(Optional.of(character));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(character.getPlayerId())).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.execute(command);

        // then
        assertThat(result.canManage()).isTrue();
        assertThat(result.isOwner()).isFalse();
    }

    private UpdatePlayerCharacter updateCommand(
            UUID characterId,
            String name,
            String personality,
            String physicalDescription) {

        return new UpdatePlayerCharacter(
                characterId, name, personality, physicalDescription, BACKGROUND, 0.25, 0.75, OWNER_USERNAME);
    }
}
