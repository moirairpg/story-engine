package me.moirai.storyengine.core.domain.world;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class WorldLorebookEntryTest {

    @Test
    public void updateLorebookEntryName() {

        // Given
        WorldLorebookEntry entry = WorldLorebookEntryFixture.sampleLorebookEntry().build();

        // When
        entry.updateName("New Name");

        // Then
        assertThat(entry.getName()).isEqualTo("New Name");
    }

    @Test
    public void updateLorebookEntryDescription() {

        // Given
        WorldLorebookEntry entry = WorldLorebookEntryFixture.sampleLorebookEntry().build();

        // When
        entry.updateDescription("New Description");

        // Then
        assertThat(entry.getDescription()).isEqualTo("New Description");
    }

    @Test
    public void updateLorebookEntryRegex() {

        // Given
        WorldLorebookEntry entry = WorldLorebookEntryFixture.sampleLorebookEntry().build();

        // When
        entry.updateRegex("[Rr]egex");

        // Then
        assertThat(entry.getRegex()).isEqualTo("[Rr]egex");
    }
}
