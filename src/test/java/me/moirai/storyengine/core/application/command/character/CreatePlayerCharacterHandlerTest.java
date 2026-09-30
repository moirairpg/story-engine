package me.moirai.storyengine.core.application.command.character;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

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
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.character.PlayerCharacter;
import me.moirai.storyengine.core.domain.character.PlayerCharacterFixture;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.inbound.character.CreatePlayerCharacter;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterRepository;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterVectorSearchPort;
import me.moirai.storyengine.core.port.outbound.generation.EmbeddingPort;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@ExtendWith(MockitoExtension.class)
public class CreatePlayerCharacterHandlerTest {

    private static final float[] VECTOR = new float[] { 0.1f, 0.2f };

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
    private CreatePlayerCharacterHandler handler;

    @Test
    void shouldSaveTheCharacterBuiltFromTheCommandWhenTheCharacterIsCreated() {

        // given
        var command = createCommand();

        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(UserFixture.NUMERIC_ID)).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        handler.execute(command);

        // then
        var saved = ArgumentCaptor.forClass(PlayerCharacter.class);
        verify(repository).save(saved.capture());

        assertThat(saved.getValue().getName()).isEqualTo("Volin Habar");
        assertThat(saved.getValue().getCharacterClass()).isEqualTo(CharacterClass.PALADIN);
        assertThat(saved.getValue().getPersonality()).isEqualTo("Brave.");
        assertThat(saved.getValue().getPhysicalDescription()).isEqualTo("Tall.");
        assertThat(saved.getValue().getBackground()).isEqualTo("Orphaned.");
        assertThat(saved.getValue().getPlayerId()).isEqualTo(UserFixture.NUMERIC_ID);
        assertThat(saved.getValue().getUiImagePositionX()).isEqualTo(0.25);
        assertThat(saved.getValue().getUiImagePositionY()).isEqualTo(0.75);
    }

    @Test
    void shouldSaveTheCharacterWithTheDistributedAttributeLevelsWhenTheCharacterIsCreated() {

        // given
        var command = createCommand();

        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(UserFixture.NUMERIC_ID)).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        handler.execute(command);

        // then
        var saved = ArgumentCaptor.forClass(PlayerCharacter.class);
        verify(repository).save(saved.capture());

        assertThat(saved.getValue().getAttributeLevels().asMap())
                .isEqualTo(PlayerCharacterFixture.sampleAttributeAllocation());
    }

    @Test
    void shouldSaveTheCharacterWithTheDistributedSkillLevelsWhenTheCharacterIsCreated() {

        // given
        var command = createCommand();

        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(UserFixture.NUMERIC_ID)).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        handler.execute(command);

        // then
        var saved = ArgumentCaptor.forClass(PlayerCharacter.class);
        verify(repository).save(saved.capture());

        assertThat(saved.getValue().getSkillLevels().asMap())
                .isEqualTo(PlayerCharacterFixture.sampleSkillAllocation());
        assertThat(saved.getValue().getSkillLevels().signature()).isEqualTo(1);
    }

    @Test
    void shouldReturnTheSkillAndSignatureLevelsWhenTheCharacterIsCreated() {

        // given
        var command = createCommand();

        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(UserFixture.NUMERIC_ID)).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.execute(command);

        // then
        assertThat(result.skills())
                .containsEntry(CharacterSkill.PERSUASION, 2)
                .containsEntry(CharacterSkill.ENDURANCE, 2);
        assertThat(result.signatureSkill()).containsEntry(SignatureSkill.ZEAL, 1);
    }

    @Test
    void shouldReturnTheAttributeLevelsWhenTheCharacterIsCreated() {

        // given
        var command = createCommand();

        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(UserFixture.NUMERIC_ID)).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.execute(command);

        // then
        assertThat(result.attributes())
                .containsEntry(CharacterAttribute.STRENGTH, 3)
                .containsEntry(CharacterAttribute.VIGOR, 2)
                .containsEntry(CharacterAttribute.CHARISMA, 1);
    }

    @Test
    void shouldIndexTheNarrativeDescriptionWhenTheCharacterIsCreated() {

        // given
        var command = createCommand();

        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(embeddingPort.embed(any())).thenReturn(VECTOR);
        when(userRepository.findById(UserFixture.NUMERIC_ID)).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        handler.execute(command);

        // then
        var embedded = ArgumentCaptor.forClass(String.class);
        verify(embeddingPort).embed(embedded.capture());

        assertThat(embedded.getValue()).isEqualTo("Volin Habar: PALADIN; Brave.; Tall.; Orphaned.");

        var saved = ArgumentCaptor.forClass(PlayerCharacter.class);
        verify(repository).save(saved.capture());
        verify(vectorSearchPort).upsert(saved.getValue().getPublicId(), VECTOR);
    }

    @Test
    void shouldReturnTheOwnerUsernameWhenTheCharacterIsCreated() {

        // given
        var command = createCommand();

        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(UserFixture.NUMERIC_ID)).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.execute(command);

        // then
        assertThat(result.ownerUsername()).isEqualTo("john.doe");
        assertThat(result.ownerDisplayName()).isEqualTo("John Doe");
        assertThat(result.name()).isEqualTo("Volin Habar");
        assertThat(result.background()).isEqualTo("Orphaned.");
    }

    @Test
    void shouldReturnBothFlagsAsTrueWhenTheCharacterIsCreated() {

        // given
        var command = createCommand();

        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(UserFixture.NUMERIC_ID)).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.execute(command);

        // then
        assertThat(result.canManage()).isTrue();
        assertThat(result.isOwner()).isTrue();
    }

    @Test
    void shouldNotIndexTheCharacterWhenTheOwnerIsNotFound() {

        // given
        var command = createCommand();

        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(UserFixture.NUMERIC_ID)).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> handler.execute(command));

        verify(embeddingPort, never()).embed(any());
        verify(vectorSearchPort, never()).upsert(any(), any());
    }

    @Test
    void shouldThrowExceptionWhenTheCommandIsMissingRequiredFields() {

        // given
        var command = new CreatePlayerCharacter(
                null, CharacterClass.PALADIN, "Brave.", "Tall.", "Orphaned.",
                PlayerCharacterFixture.sampleAttributeAllocation(),
                PlayerCharacterFixture.sampleSkillAllocation(),
                PlayerCharacterFixture.sampleSignatureAllocation(), 0.25, 0.75, UserFixture.NUMERIC_ID);

        // then
        assertThrows(RuntimeException.class, () -> handler.execute(command));

        verify(repository, never()).save(any());
        verify(embeddingPort, never()).embed(any());
        verify(vectorSearchPort, never()).upsert(any(), any());
    }

    private CreatePlayerCharacter createCommand() {

        return new CreatePlayerCharacter(
                "Volin Habar",
                CharacterClass.PALADIN,
                "Brave.",
                "Tall.",
                "Orphaned.",
                PlayerCharacterFixture.sampleAttributeAllocation(),
                PlayerCharacterFixture.sampleSkillAllocation(),
                PlayerCharacterFixture.sampleSignatureAllocation(),
                0.25,
                0.75,
                UserFixture.NUMERIC_ID);
    }
}
