package me.moirai.storyengine.core.application.command.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.context.ApplicationEventPublisher;

import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntryFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntryRemovedEvent;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventureLorebookEntry;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@ExtendWith(MockitoExtension.class)
public class DeleteAdventureLorebookEntryHandlerTest {

    @Mock
    private AdventureRepository repository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DeleteAdventureLorebookEntryHandler handler;

    @Test
    public void errorWhenEntryIdIsNull() {

        // given
        var command = new DeleteAdventureLorebookEntry(
                null,
                AdventureFixture.PUBLIC_ID);

        // then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void errorWhenAdventureIdIsNull() {

        // given
        var command = new DeleteAdventureLorebookEntry(
                AdventureLorebookEntryFixture.PUBLIC_ID,
                null);

        // then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void shouldRemoveTheEntrySaveAndPublishTheRemovalWhenTheEntryExists() {

        // given
        var adventure = AdventureFixture.publicAdventure().build();
        var entry = adventure.addLorebookEntry("Name", "Description");

        var command = new DeleteAdventureLorebookEntry(entry.getPublicId(), AdventureFixture.PUBLIC_ID);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));

        // when
        handler.handle(command);

        // then
        assertThat(adventure.getLorebook()).isEmpty();
        verify(repository).save(adventure);

        var publishedEvent = ArgumentCaptor.forClass(AdventureLorebookEntryRemovedEvent.class);
        verify(eventPublisher).publishEvent(publishedEvent.capture());

        assertThat(publishedEvent.getValue().getEntryId()).isEqualTo(entry.getPublicId());
    }

    @Test
    public void shouldThrowWhenAdventureNotFoundOnDelete() {

        // given
        var command = new DeleteAdventureLorebookEntry(
                AdventureLorebookEntryFixture.PUBLIC_ID,
                AdventureFixture.PUBLIC_ID);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> handler.handle(command));

        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }
}
