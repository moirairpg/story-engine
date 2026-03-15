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

import me.moirai.storyengine.core.application.usecase.adventure.request.CreateAdventureLorebookEntryFixture;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventureLorebookEntry;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationResult;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class CreateAdventureLorebookEntryHandlerTest {

    @Mock
    private TextModerationPort moderationPort;

    @Mock
    private AdventureRepository repository;

    @InjectMocks
    private CreateAdventureLorebookEntryHandler handler;

    @Test
    public void createEntry_whenAdventureIdIsNull_thenThrowException() {

        // Given
        CreateAdventureLorebookEntry command = CreateAdventureLorebookEntryFixture.sampleLorebookEntry()
                .adventureId(null)
                .build();

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void createEntry_whenNameIdIsNull_thenThrowException() {

        // Given
        CreateAdventureLorebookEntry command = CreateAdventureLorebookEntryFixture.sampleLorebookEntry()
                .name(null)
                .build();

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void createEntry_whenDescriptionIdIsNull_thenThrowException() {

        // Given
        CreateAdventureLorebookEntry command = CreateAdventureLorebookEntryFixture.sampleLorebookEntry()
                .description(null)
                .build();

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void createEntry_whenTriggered_thenCallService() {

        // Given
        String requesterId = "1234";
        CreateAdventureLorebookEntry command = CreateAdventureLorebookEntryFixture.samplePlayerCharacterLorebookEntry()
                .requesterId(requesterId)
                .build();

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        TextModerationResult moderationResult = TextModerationResult.builder()
                .contentFlagged(false)
                .build();

        when(repository.findByPublicId(anyString())).thenReturn(Optional.of(adventure));
        when(moderationPort.moderate(anyString())).thenReturn(Mono.just(moderationResult));
        when(repository.save(any())).thenReturn(adventure);

        // Then
        StepVerifier.create(handler.handle(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getName()).isEqualTo(command.getName());
                })
                .verifyComplete();
    }
}
