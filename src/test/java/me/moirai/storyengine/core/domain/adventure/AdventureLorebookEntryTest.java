package me.moirai.storyengine.core.domain.adventure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class AdventureLorebookEntryTest {

    @Test
    public void createLorebookEntry_whenValidData_thenInstanceIsCreated() {

        // Given
        AdventureLorebookEntry.Builder builder = AdventureLorebookEntry.builder()
                .name("White River")
                .description("The White River goes through Falkreath, Whiterun and ends in Eastmarch.")
                .regex("[Ww]hite [Rr]iver")
                .playerId("2423423423423");

        // When
        AdventureLorebookEntry entry = builder.build();
        ReflectionTestUtils.setField(entry, "id", AdventureLorebookEntryFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(entry, "publicId", AdventureLorebookEntryFixture.PUBLIC_ID);

        // Then
        assertThat(entry).isNotNull();
        assertThat(entry.getDescription()).isEqualTo("The White River goes through Falkreath, Whiterun and ends in Eastmarch.");
        assertThat(entry.getPublicId()).isEqualTo(AdventureLorebookEntryFixture.PUBLIC_ID);
        assertThat(entry.getName()).isEqualTo("White River");
        assertThat(entry.getPlayerId()).isEqualTo("2423423423423");
        assertThat(entry.getRegex()).isEqualTo("[Ww]hite [Rr]iver");
        assertThat(entry.isPlayerCharacter()).isTrue();
    }

    @Test
    public void updateLorebookEntryName() {

        // Given
        AdventureLorebookEntry entry = AdventureLorebookEntryFixture.sampleLorebookEntry().build();

        // When
        entry.updateName("New Name");

        // Then
        assertThat(entry.getName()).isEqualTo("New Name");
    }

    @Test
    public void updateLorebookEntryDescription() {

        // Given
        AdventureLorebookEntry entry = AdventureLorebookEntryFixture.sampleLorebookEntry().build();

        // When
        entry.updateDescription("New Description");

        // Then
        assertThat(entry.getDescription()).isEqualTo("New Description");
    }

    @Test
    public void updateLorebookEntryRegex() {

        // Given
        AdventureLorebookEntry entry = AdventureLorebookEntryFixture.sampleLorebookEntry().build();

        // When
        entry.updateRegex("[Rr]egex");

        // Then
        assertThat(entry.getRegex()).isEqualTo("[Rr]egex");
    }

    @Test
    public void assignUserToLorebookEntry() {

        // Given
        String expectedPlayerDiscordId = "4234234234";
        AdventureLorebookEntry entry = AdventureLorebookEntryFixture.sampleLorebookEntry().build();

        // When
        entry.assignPlayer(expectedPlayerDiscordId);

        // Then
        assertThat(expectedPlayerDiscordId).isEqualTo(entry.getPlayerId());
    }

    @Test
    public void unassignUserToLorebookEntry() {

        // Given
        AdventureLorebookEntry entry = AdventureLorebookEntryFixture.samplePlayerCharacterLorebookEntry().build();

        // When
        entry.unassignPlayer();

        // Then
        assertThat(entry.getPlayerId()).isBlank();
    }
}
