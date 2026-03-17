package me.moirai.storyengine.core.application.usecase.adventure.request;

import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventureLorebookEntry;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class CreateAdventureLorebookEntryTest {

    @Test
    public void createEntryCommand_whenValidData_thenBuildNewInstance() {

        // Given / When
        CreateAdventureLorebookEntry command = new CreateAdventureLorebookEntry(
                AdventureFixture.PUBLIC_ID,
                "Volin Habar",
                "[Vv]olin [Hh]abar|[Vv]oha",
                "Volin Habar is a warrior that fights with a sword.",
                "2423423423423",
                "1234");

        // Then
        assertThat(command).isNotNull();
        assertThat(command.adventureId()).isEqualTo(AdventureFixture.PUBLIC_ID);
        assertThat(command.name()).isEqualTo("Volin Habar");
        assertThat(command.description()).isEqualTo("Volin Habar is a warrior that fights with a sword.");
        assertThat(command.regex()).isEqualTo("[Vv]olin [Hh]abar|[Vv]oha");
        assertThat(command.playerId()).isEqualTo("2423423423423");
        assertThat(command.requesterId()).isEqualTo("1234");
    }
}
