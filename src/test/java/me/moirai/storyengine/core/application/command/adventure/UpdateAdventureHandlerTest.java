package me.moirai.storyengine.core.application.command.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntryRemovedEvent;
import me.moirai.storyengine.core.port.inbound.UpdateAdventureFixture;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.adventure.LorebookVectorSearchPort;
import me.moirai.storyengine.core.port.outbound.generation.EmbeddingPort;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;

@ExtendWith(MockitoExtension.class)
public class UpdateAdventureHandlerTest {

    @Mock
    private AdventureRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmbeddingPort embeddingPort;

    @Mock
    private LorebookVectorSearchPort vectorSearchPort;

    @Mock
    private StoragePort storagePort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UpdateAdventureHandler handler;

    @Test
    public void errorWhenIdIsNull() {

        // given
        var command = new UpdateAdventure(
                null,
                null, null, null, null, null, null, null,
                null, null, null, null, List.of(), List.of(), List.of(), UserFixture.PUBLIC_ID);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void updateAdventure() {

        // given
        var requesterId = "DASDASD";
        var command = UpdateAdventureFixture.sampleWithRequesterId(requesterId);

        var expectedUpdatedAdventure = AdventureFixture.privateAdventure().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(expectedUpdatedAdventure));
        when(repository.save(any())).thenReturn(expectedUpdatedAdventure);
        when(userRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.lastUpdateDate()).isEqualTo(expectedUpdatedAdventure.getLastUpdateDate());
    }

    @Test
    public void shouldPersistEveryModelConfigurationFieldWhenTheAdventureIsUpdated() {

        // given
        var command = UpdateAdventureFixture.sampleWithModelConfiguration(
                ArtificialIntelligenceModel.GPT54, 100000, 1.4);

        var adventure = AdventureFixture.privateAdventure().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(repository.save(any())).thenReturn(adventure);
        when(userRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        handler.handle(command);

        // then
        var saved = ArgumentCaptor.forClass(Adventure.class);
        verify(repository).save(saved.capture());

        var modelConfiguration = saved.getValue().getModelConfiguration();

        assertThat(modelConfiguration.getAiModel()).isEqualTo(ArtificialIntelligenceModel.GPT54);
        assertThat(modelConfiguration.getMaxTokenLimit()).isEqualTo(100000);
        assertThat(modelConfiguration.getTemperature()).isEqualTo(1.4);
    }

    @Test
    public void shouldSwitchToAModelWithASmallerCapWhenTheLimitIsLoweredInTheSameUpdate() {

        // given
        var adventure = AdventureFixture.privateAdventure().build();

        adventure.updateModelConfiguration(ArtificialIntelligenceModel.GPT54, 100000, 1.0);

        var command = UpdateAdventureFixture.sampleWithModelConfiguration(
                ArtificialIntelligenceModel.GPT54_MINI, 40000, 1.0);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(repository.save(any())).thenReturn(adventure);
        when(userRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        handler.handle(command);

        // then
        var saved = ArgumentCaptor.forClass(Adventure.class);
        verify(repository).save(saved.capture());

        assertThat(saved.getValue().getModelConfiguration().getAiModel())
                .isEqualTo(ArtificialIntelligenceModel.GPT54_MINI);
        assertThat(saved.getValue().getModelConfiguration().getMaxTokenLimit()).isEqualTo(40000);
    }

    @Test
    public void shouldDisableRpgMechanicsWhenTheAdventureIsUpdatedWithTheFlagOff() {

        // given
        var command = UpdateAdventureFixture.sampleWithRpgMechanicsDisabled();
        var adventure = AdventureFixture.privateAdventure().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(repository.save(any(Adventure.class))).thenReturn(adventure);
        when(userRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        var saved = ArgumentCaptor.forClass(Adventure.class);
        verify(repository).save(saved.capture());

        assertThat(saved.getValue().isRpgMechanicsEnabled()).isFalse();
        assertThat(result.rpgMechanicsEnabled()).isFalse();
    }

    @Test
    public void shouldKeepRpgMechanicsEnabledWhenTheAdventureIsUpdatedWithTheFlagOn() {

        // given
        var command = UpdateAdventureFixture.sample();
        var adventure = AdventureFixture.privateAdventure().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(repository.save(any(Adventure.class))).thenReturn(adventure);
        when(userRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        var saved = ArgumentCaptor.forClass(Adventure.class);
        verify(repository).save(saved.capture());

        assertThat(saved.getValue().isRpgMechanicsEnabled()).isTrue();
        assertThat(result.rpgMechanicsEnabled()).isTrue();
    }

    @Test
    public void shouldEnableRpgMechanicsWhenTheFlagIsNull() {

        // given
        var command = UpdateAdventureFixture.sampleWithRpgMechanicsNull();
        var adventure = AdventureFixture.privateAdventure().build();

        adventure.updateRpgMechanicsEnabled(false);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(repository.save(any(Adventure.class))).thenReturn(adventure);
        when(userRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        var saved = ArgumentCaptor.forClass(Adventure.class);
        verify(repository).save(saved.capture());

        assertThat(saved.getValue().isRpgMechanicsEnabled()).isTrue();
        assertThat(result.rpgMechanicsEnabled()).isTrue();
    }

    @Test
    public void updateAdventure_whenAdventureToUpdateNotFound_thenThrowException() {

        // given
        var requesterUserId = "LALALA";
        var updateAdventure = UpdateAdventureFixture.sampleWithRequesterId(requesterUserId);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> handler.execute(updateAdventure));
    }

    @Test
    public void shouldUpdateNarratorWhenNarratorFieldsAreProvided() {

        // given
        var sample = UpdateAdventureFixture.sample();
        var command = new UpdateAdventure(
                sample.adventureId(),
                sample.name(),
                sample.description(),
                sample.adventureStart(),
                "Elan",
                "A wise elder narrator",
                sample.moderation(),
                true,
                null,
                null,
                sample.modelConfiguration(),
                sample.contextAttributes(),
                List.of(),
                List.of(),
                List.of(),
                UserFixture.PUBLIC_ID);

        var adventure = AdventureFixture.privateAdventure().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(repository.save(any(Adventure.class))).thenReturn(adventure);
        when(userRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    public void updateAdventure_whenLorebookEntriesToAdd_thenVectorsAreUpserted() {

        // given
        var sample = UpdateAdventureFixture.sample();
        var command = new UpdateAdventure(
                AdventureFixture.PUBLIC_ID,
                "MoirAI",
                "Description",
                "Adventure start",
                null,
                null,
                Moderation.PERMISSIVE,
                true,
                null,
                null,
                sample.modelConfiguration(),
                sample.contextAttributes(),
                List.of(new UpdateAdventure.LorebookEntryToAdd("Hero", "The main character")),
                List.of(),
                List.of(),
                UserFixture.PUBLIC_ID);

        var adventure = AdventureFixture.privateAdventureWithoutNarrator().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        doAnswer(inv -> {
            var saved = inv.getArgument(0, Adventure.class);
            ReflectionTestUtils.setField(saved, "publicId", AdventureFixture.PUBLIC_ID);
            saved.getLorebook().forEach(e -> ReflectionTestUtils.setField(e, "publicId", UUID.randomUUID()));
            return saved;
        }).when(repository).save(any(Adventure.class));
        when(embeddingPort.embedAll(anyList())).thenReturn(List.of(new float[]{0.1f}));
        when(userRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        verify(embeddingPort).embedAll(anyList());
        verify(vectorSearchPort).upsert(any(UUID.class), any(UUID.class), any(float[].class));
    }

    @Test
    public void updateAdventure_whenLorebookEntriesToUpdate_thenVectorsAreUpserted() {

        // given
        var sample = UpdateAdventureFixture.sample();
        var adventure = AdventureFixture.privateAdventureWithoutNarrator().build();
        var addedEntry = adventure.addLorebookEntry("Old Name", "Old Description");
        var entryId = UUID.randomUUID();
        ReflectionTestUtils.setField(addedEntry, "publicId", entryId);

        var command = new UpdateAdventure(
                AdventureFixture.PUBLIC_ID,
                "MoirAI",
                "Description",
                "Adventure start",
                null,
                null,
                Moderation.PERMISSIVE,
                true,
                null,
                null,
                sample.modelConfiguration(),
                sample.contextAttributes(),
                List.of(),
                List.of(new UpdateAdventure.LorebookEntryToUpdate(entryId, "New Name", "New Description")),
                List.of(),
                UserFixture.PUBLIC_ID);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        doAnswer(inv -> {
            var saved = inv.getArgument(0, Adventure.class);
            ReflectionTestUtils.setField(saved, "publicId", AdventureFixture.PUBLIC_ID);
            return saved;
        }).when(repository).save(any(Adventure.class));
        when(embeddingPort.embedAll(anyList())).thenReturn(List.of(new float[]{0.1f}));
        when(userRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        verify(embeddingPort).embedAll(anyList());
        verify(vectorSearchPort).upsert(any(UUID.class), any(UUID.class), any(float[].class));
    }

    @Test
    public void updateAdventure_whenLorebookEntriesToDelete_thenRemovalIsPublishedAndVectorsAreNotDeletedDirectly() {

        // given
        var sample = UpdateAdventureFixture.sample();
        var entryId = UUID.randomUUID();
        var command = new UpdateAdventure(
                AdventureFixture.PUBLIC_ID,
                "MoirAI",
                "Description",
                "Adventure start",
                null,
                null,
                Moderation.PERMISSIVE,
                true,
                null,
                null,
                sample.modelConfiguration(),
                sample.contextAttributes(),
                List.of(),
                List.of(),
                List.of(entryId),
                UserFixture.PUBLIC_ID);

        var adventure = AdventureFixture.privateAdventureWithoutNarrator().build();
        var addedEntry = adventure.addLorebookEntry("Entry", "Description");
        ReflectionTestUtils.setField(addedEntry, "publicId", entryId);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(repository.save(any(Adventure.class))).thenReturn(adventure);
        when(userRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(adventure.getLorebook()).isEmpty();

        var publishedEvent = ArgumentCaptor.forClass(AdventureLorebookEntryRemovedEvent.class);
        verify(eventPublisher).publishEvent(publishedEvent.capture());

        assertThat(publishedEvent.getValue().getEntryId()).isEqualTo(entryId);
        verify(vectorSearchPort, never()).delete(any());
    }

    @Test
    public void updateAdventure_whenFocalPointProvided_thenFocalPointIsUpdated() {

        // given
        var sample = UpdateAdventureFixture.sample();
        var command = new UpdateAdventure(
                AdventureFixture.PUBLIC_ID,
                "MoirAI",
                "Description",
                "Adventure start",
                null,
                null,
                Moderation.PERMISSIVE,
                true,
                0.3,
                0.7,
                sample.modelConfiguration(),
                sample.contextAttributes(),
                List.of(),
                List.of(),
                List.of(),
                UserFixture.PUBLIC_ID);

        var adventure = AdventureFixture.privateAdventureWithoutNarrator().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(repository.save(any(Adventure.class))).thenReturn(adventure);
        when(userRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        verify(repository).save(any(Adventure.class));
    }

    @Test
    public void shouldReturnIsOwnerWhenTheRequesterHoldsTheOwnerPermission() {

        // given
        var command = UpdateAdventureFixture.sample();
        var adventure = AdventureFixture.privateAdventure().build();

        var requester = UserFixture.player().build();
        ReflectionTestUtils.setField(requester, "id", AdventureFixture.OWNER_ID);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(repository.save(any(Adventure.class))).thenReturn(adventure);
        when(userRepository.findByPublicId(UserFixture.PUBLIC_ID)).thenReturn(Optional.of(requester));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result.canManage()).isTrue();
        assertThat(result.isOwner()).isTrue();
    }

    @Test
    public void shouldNotReturnIsOwnerWhenTheRequesterDoesNotHoldTheOwnerPermission() {

        // given
        var command = UpdateAdventureFixture.sample();
        var adventure = AdventureFixture.privateAdventure().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(repository.save(any(Adventure.class))).thenReturn(adventure);
        when(userRepository.findByPublicId(UserFixture.PUBLIC_ID)).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result.canManage()).isTrue();
        assertThat(result.isOwner()).isFalse();
    }
}
