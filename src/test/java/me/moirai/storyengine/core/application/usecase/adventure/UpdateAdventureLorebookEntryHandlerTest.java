package me.moirai.storyengine.core.application.usecase.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.application.usecase.adventure.request.UpdateAdventureLorebookEntry;
import me.moirai.storyengine.core.application.usecase.adventure.request.UpdateAdventureLorebookEntryFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntry;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntryFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureService;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class UpdateAdventureLorebookEntryHandlerTest {

    @Mock
    private AdventureService domainService;

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
        UpdateAdventureLorebookEntry command = UpdateAdventureLorebookEntryFixture.samplePlayerCharacterLorebookEntry()
                .build();
        AdventureLorebookEntry createdEntry = AdventureLorebookEntryFixture.sampleLorebookEntry()
                .id(id)
                .build();

        when(domainService.updateLorebookEntry(any())).thenReturn(Mono.just(createdEntry));

        // Then
        StepVerifier.create(handler.handle(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getLastUpdatedDateTime()).isNotNull();
                })
                .verifyComplete();
    }
}
