package me.moirai.storyengine.core.application.command.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.port.inbound.CreateAdventureLorebookEntryFixture;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventureLorebookEntry;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.generation.EmbeddingPort;
import me.moirai.storyengine.core.port.outbound.vectorsearch.LorebookVectorSearchPort;

@ExtendWith(MockitoExtension.class)
public class CreateAdventureLorebookEntryHandlerTest {

    @Mock
    private AdventureRepository repository;

    @Mock
    private EmbeddingPort embeddingPort;

    @Mock
    private LorebookVectorSearchPort vectorSearchPort;

    @InjectMocks
    private CreateAdventureLorebookEntryHandler handler;

    @Test
    public void createEntry_whenAdventureIdIsNull_thenThrowException() {

        // given
        var command = new CreateAdventureLorebookEntry(
                null,
                "Volin Habar",
                "Volin Habar is a warrior that fights with a sword.",
                null);

        // then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void createEntry_whenNameIdIsNull_thenThrowException() {

        // given
        var command = new CreateAdventureLorebookEntry(
                AdventureFixture.PUBLIC_ID,
                null,
                "Volin Habar is a warrior that fights with a sword.",
                null);

        // then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void createEntry_whenDescriptionIdIsNull_thenThrowException() {

        // given
        var command = new CreateAdventureLorebookEntry(
                AdventureFixture.PUBLIC_ID,
                "Volin Habar",
                null,
                null);

        // then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void shouldUpsertVectorAfterSavingEntryWhenCreateSucceeds() {

        // given
        var command = CreateAdventureLorebookEntryFixture.samplePlayerCharacterLorebookEntry();
        var adventure = AdventureFixture.privateMultiplayerAdventure().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(repository.save(any())).thenReturn(adventure);
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(command.name());
        verify(repository, times(1)).save(any());
        verify(embeddingPort, times(1)).embed(anyString());
        verify(vectorSearchPort, times(1)).upsert(any(), any(), any(float[].class));
    }

    @Test
    public void shouldThrowWhenAdventureNotFoundOnCreate() {

        // given
        var command = CreateAdventureLorebookEntryFixture.samplePlayerCharacterLorebookEntry();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> handler.handle(command));
        verify(embeddingPort, times(0)).embed(anyString());
        verify(vectorSearchPort, times(0)).upsert(any(), any(), any());
    }
}
