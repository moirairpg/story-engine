package me.moirai.storyengine.core.application.command.character;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.enums.CharacterClass;
import me.moirai.storyengine.common.enums.CharacterSkill;
import me.moirai.storyengine.common.enums.SignatureSkill;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.character.PlayerCharacterFixture;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.inbound.character.UpdateCharacterSheet;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterRepository;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterVectorSearchPort;
import me.moirai.storyengine.core.port.outbound.generation.EmbeddingPort;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UpdateCharacterSheetHandlerTest {

    private static final float[] VECTOR = new float[] { 0.1f, 0.2f };
    private static final String OWNER_USERNAME = "john.doe";

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
    private UpdateCharacterSheetHandler handler;

    @Test
    void shouldApplyTheFullSheetWhenTheSheetIsUpdated() {

        // given
        var character = PlayerCharacterFixture.samplePlayerCharacterWithId();
        var command = new UpdateCharacterSheet(
                character.getPublicId(), CharacterClass.MAGE,
                PlayerCharacterFixture.sampleAttributeAllocation(),
                PlayerCharacterFixture.skillAllocationFor(CharacterClass.MAGE),
                PlayerCharacterFixture.signatureAllocationFor(CharacterClass.MAGE),
                OWNER_USERNAME);

        when(repository.findByPublicId(character.getPublicId())).thenReturn(Optional.of(character));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(embeddingPort.embed(any())).thenReturn(VECTOR);
        when(userRepository.findById(character.getPlayerId())).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.execute(command);

        // then
        assertThat(result.characterClass()).isEqualTo(CharacterClass.MAGE);
        assertThat(result.skills())
                .containsEntry(CharacterSkill.DESTRUCTION, 2)
                .containsEntry(CharacterSkill.CONJURATION, 2);
        assertThat(result.signatureSkill()).containsEntry(SignatureSkill.SPELLWEAVE, 1);
        assertThat(result.background()).isEqualTo(character.getBackground());
        assertThat(result.ownerUsername()).isEqualTo(OWNER_USERNAME);
        assertThat(result.ownerDisplayName()).isEqualTo("John Doe");

        verify(vectorSearchPort).upsert(character.getPublicId(), VECTOR);
    }

    @Test
    void shouldThrowExceptionWhenTheSheetIsIncomplete() {

        // given
        var character = PlayerCharacterFixture.samplePlayerCharacterWithId();
        var command = new UpdateCharacterSheet(
                character.getPublicId(), CharacterClass.PALADIN,
                PlayerCharacterFixture.sampleAttributeAllocation(), null,
                PlayerCharacterFixture.sampleSignatureAllocation(), OWNER_USERNAME);

        when(repository.findByPublicId(character.getPublicId())).thenReturn(Optional.of(character));
        when(userRepository.findById(character.getPlayerId())).thenReturn(Optional.of(UserFixture.playerWithId()));

        // then
        assertThrows(BusinessRuleViolationException.class, () -> handler.execute(command));

        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenTheSheetHasNoClass() {

        // given
        var character = PlayerCharacterFixture.samplePlayerCharacterWithId();
        var command = new UpdateCharacterSheet(
                character.getPublicId(), null,
                PlayerCharacterFixture.sampleAttributeAllocation(),
                PlayerCharacterFixture.sampleSkillAllocation(),
                PlayerCharacterFixture.sampleSignatureAllocation(), OWNER_USERNAME);

        when(repository.findByPublicId(character.getPublicId())).thenReturn(Optional.of(character));
        when(userRepository.findById(character.getPlayerId())).thenReturn(Optional.of(UserFixture.playerWithId()));

        // then
        assertThrows(BusinessRuleViolationException.class, () -> handler.execute(command));

        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenTheCharacterIsNotFound() {

        // given
        var command = new UpdateCharacterSheet(
                UUID.randomUUID(), CharacterClass.PALADIN,
                PlayerCharacterFixture.sampleAttributeAllocation(),
                PlayerCharacterFixture.sampleSkillAllocation(),
                PlayerCharacterFixture.sampleSignatureAllocation(), OWNER_USERNAME);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> handler.execute(command));

        verify(repository, never()).save(any());
        verify(vectorSearchPort, never()).upsert(any(), any());
    }
}
