package me.moirai.storyengine.core.application.command.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
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
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntry;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntryFixture;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureLorebookEntry;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationResult;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class UpdateAdventureLorebookEntryHandlerTest {

    @Mock
    private TextModerationPort moderationPort;

    @Mock
    private AdventureRepository repository;

    @InjectMocks
    private UpdateAdventureLorebookEntryHandler handler;

    @Test
    public void createEntry_whenEntryIdIsNull_thenThrowException() {

        // Given
        UpdateAdventureLorebookEntry command = new UpdateAdventureLorebookEntry(
                null,
                AdventureFixture.PUBLIC_ID,
                "Volin Habar",
                "[Vv]olin [Hh]abar|[Vv]oha",
                "Volin Habar is a warrior that fights with a sword.",
                null,
                "1234");

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void createEntry_whenAdventureIdIsNull_thenThrowException() {

        // Given
        UpdateAdventureLorebookEntry command = new UpdateAdventureLorebookEntry(
                AdventureLorebookEntryFixture.PUBLIC_ID,
                null,
                "Volin Habar",
                "[Vv]olin [Hh]abar|[Vv]oha",
                "Volin Habar is a warrior that fights with a sword.",
                null,
                "1234");

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void createEntry_whenNameIdIsNull_thenThrowException() {

        // Given
        UpdateAdventureLorebookEntry command = new UpdateAdventureLorebookEntry(
                AdventureLorebookEntryFixture.PUBLIC_ID,
                AdventureFixture.PUBLIC_ID,
                null,
                "[Vv]olin [Hh]abar|[Vv]oha",
                "Volin Habar is a warrior that fights with a sword.",
                null,
                "1234");

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void createEntry_whenDescriptionIdIsNull_thenThrowException() {

        // Given
        UpdateAdventureLorebookEntry command = new UpdateAdventureLorebookEntry(
                AdventureLorebookEntryFixture.PUBLIC_ID,
                AdventureFixture.PUBLIC_ID,
                "Volin Habar",
                "[Vv]olin [Hh]abar|[Vv]oha",
                null,
                null,
                "1234");

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void createEntry_whenTriggered_thenCallService() {

        // Given
        String requesterId = "1234";
        UpdateAdventureLorebookEntry command = new UpdateAdventureLorebookEntry(
                AdventureLorebookEntryFixture.PUBLIC_ID,
                AdventureFixture.PUBLIC_ID,
                "Volin Habar",
                "[Vv]olin [Hh]abar|[Vv]oha",
                "Volin Habar is a warrior that fights with a sword.",
                "2423423423423",
                requesterId);

        AdventureLorebookEntry existingEntry = AdventureLorebookEntryFixture.sampleLorebookEntry().build();

        Adventure baseAdventure = AdventureFixture.privateMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        Adventure adventure = spy(baseAdventure);
        doReturn(existingEntry).when(adventure).updateLorebookEntry(any(UUID.class), anyString(), anyString(), anyString(), anyString());

        TextModerationResult moderationResult = TextModerationResult.builder()
                .contentFlagged(false)
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(moderationPort.moderate(anyString())).thenReturn(Mono.just(moderationResult));
        when(repository.save(any())).thenReturn(adventure);

        // Then
        StepVerifier.create(handler.handle(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.lastUpdateDate()).isNotNull();
                })
                .verifyComplete();
    }
}
