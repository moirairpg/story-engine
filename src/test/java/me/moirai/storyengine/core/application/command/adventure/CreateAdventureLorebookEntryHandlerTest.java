package me.moirai.storyengine.core.application.command.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.port.inbound.CreateAdventureLorebookEntryFixture;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventureLorebookEntry;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@ExtendWith(MockitoExtension.class)
public class CreateAdventureLorebookEntryHandlerTest {

    @Mock
    private AdventureRepository repository;

    @InjectMocks
    private CreateAdventureLorebookEntryHandler handler;

    @Test
    public void createEntry_whenAdventureIdIsNull_thenThrowException() {

        // given
        var command = new CreateAdventureLorebookEntry(
                null,
                "Volin Habar",
                "[Vv]olin [Hh]abar|[Vv]oha",
                "Volin Habar is a warrior that fights with a sword.",
                null);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void createEntry_whenNameIdIsNull_thenThrowException() {

        // given
        var command = new CreateAdventureLorebookEntry(
                AdventureFixture.PUBLIC_ID,
                null,
                "[Vv]olin [Hh]abar|[Vv]oha",
                "Volin Habar is a warrior that fights with a sword.",
                null);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void createEntry_whenDescriptionIdIsNull_thenThrowException() {

        // given
        var command = new CreateAdventureLorebookEntry(
                AdventureFixture.PUBLIC_ID,
                "Volin Habar",
                "[Vv]olin [Hh]abar|[Vv]oha",
                null,
                null);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void createEntry_whenTriggered_thenCallService() {

        // given
        var command = CreateAdventureLorebookEntryFixture.samplePlayerCharacterLorebookEntry();

        var adventure = AdventureFixture.privateMultiplayerAdventure().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(repository.save(any())).thenReturn(adventure);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(command.name());
    }
}
