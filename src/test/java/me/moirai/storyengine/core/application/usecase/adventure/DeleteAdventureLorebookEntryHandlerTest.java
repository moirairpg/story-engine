package me.moirai.storyengine.core.application.usecase.adventure;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
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

import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntryFixture;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventureLorebookEntry;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@ExtendWith(MockitoExtension.class)
public class DeleteAdventureLorebookEntryHandlerTest {

    @Mock
    private AdventureRepository repository;

    @InjectMocks
    private DeleteAdventureLorebookEntryHandler handler;

    @Test
    public void errorWhenEntryIdIsNull() {

        // Given
        DeleteAdventureLorebookEntry command = new DeleteAdventureLorebookEntry(
                null,
                AdventureFixture.PUBLIC_ID,
                "RQSTRID");

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void errorWhenAdventureIdIsNull() {

        // Given
        DeleteAdventureLorebookEntry command = new DeleteAdventureLorebookEntry(
                AdventureLorebookEntryFixture.PUBLIC_ID,
                null,
                "RQSTRID");

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void deleteAdventure() {

        // Given
        String requesterId = "4234324";

        DeleteAdventureLorebookEntry command = new DeleteAdventureLorebookEntry(
                AdventureLorebookEntryFixture.PUBLIC_ID,
                AdventureFixture.PUBLIC_ID,
                requesterId);

        Adventure baseAdventure = AdventureFixture.publicMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        Adventure adventure = spy(baseAdventure);
        doNothing().when(adventure).removeLorebookEntry(any(UUID.class));

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(repository.save(any())).thenReturn(adventure);

        // When
        handler.handle(command);

        // Then
        verify(repository, times(1)).save(any());
    }
}
