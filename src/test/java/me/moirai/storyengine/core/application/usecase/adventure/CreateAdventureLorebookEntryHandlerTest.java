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

import me.moirai.storyengine.core.application.usecase.adventure.request.CreateAdventureLorebookEntry;
import me.moirai.storyengine.core.application.usecase.adventure.request.CreateAdventureLorebookEntryFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntry;
import me.moirai.storyengine.core.domain.adventure.AdventureService;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class CreateAdventureLorebookEntryHandlerTest {

    @Mock
    private AdventureService domainService;

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
        String id = "LBID";
        CreateAdventureLorebookEntry command = CreateAdventureLorebookEntryFixture.samplePlayerCharacterLorebookEntry().build();
        AdventureLorebookEntry createdEntry = AdventureLorebookEntry.builder()
                .id(id)
                .build();

        when(domainService.createLorebookEntry(any())).thenReturn(Mono.just(createdEntry));

        // Then
        StepVerifier.create(handler.handle(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getId()).isEqualTo(id);
                })
                .verifyComplete();
    }
}
