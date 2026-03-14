package me.moirai.storyengine.core.application.usecase.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.application.usecase.adventure.request.UpdateAdventureLorebookEntryFixture;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntry;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntryFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntryRepository;
import me.moirai.storyengine.core.domain.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.TextModerationPort;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureLorebookEntry;
import me.moirai.storyengine.core.port.outbound.TextModerationResult;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class UpdateAdventureLorebookEntryHandlerTest {

    @Mock
    private TextModerationPort moderationPort;

    @Mock
    private AdventureLorebookEntryRepository lorebookEntryRepository;

    @Mock
    private AdventureRepository repository;

    @InjectMocks
    private UpdateAdventureLorebookEntryHandler handler;

    @Test
    public void createEntry_whenEntryIdIsNull_thenThrowException() {

        // Given
        UpdateAdventureLorebookEntry command = UpdateAdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(null)
                .build();

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void createEntry_whenAdventureIdIsNull_thenThrowException() {

        // Given
        UpdateAdventureLorebookEntry command = UpdateAdventureLorebookEntryFixture.sampleLorebookEntry()
                .adventureId(null)
                .build();

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void createEntry_whenNameIdIsNull_thenThrowException() {

        // Given
        UpdateAdventureLorebookEntry command = UpdateAdventureLorebookEntryFixture.sampleLorebookEntry()
                .name(null)
                .build();

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void createEntry_whenDescriptionIdIsNull_thenThrowException() {

        // Given
        UpdateAdventureLorebookEntry command = UpdateAdventureLorebookEntryFixture.sampleLorebookEntry()
                .description(null)
                .build();

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void createEntry_whenTriggered_thenCallService() {

        // Given
        String id = "LBID";
        String requesterId = "1234";
        UpdateAdventureLorebookEntry command = UpdateAdventureLorebookEntryFixture.samplePlayerCharacterLorebookEntry()
                .requesterId(requesterId)
                .build();

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        AdventureLorebookEntry createdEntry = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(id)
                .build();

        TextModerationResult moderationResult = TextModerationResult.builder()
                .contentFlagged(false)
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(adventure));
        when(moderationPort.moderate(anyString())).thenReturn(Mono.just(moderationResult));
        when(lorebookEntryRepository.findById(anyString())).thenReturn(Optional.of(createdEntry));
        when(lorebookEntryRepository.save(any())).thenReturn(createdEntry);

        // Then
        StepVerifier.create(handler.handle(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getLastUpdatedDateTime()).isNotNull();
                })
                .verifyComplete();
    }
}
