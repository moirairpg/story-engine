package me.moirai.storyengine.infrastructure.outbound.adapter.world;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;
import me.moirai.storyengine.core.port.outbound.world.WorldLorebookReader;

public class WorldLorebookReaderImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WorldLorebookReader reader;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void getWorldLorebookEntryById_whenNotFound_thenReturnEmpty() {

        // Given
        var entryPublicId = UUID.randomUUID();
        var worldPublicId = UUID.randomUUID();

        // When
        var result = reader.getWorldLorebookEntryById(entryPublicId, worldPublicId);

        // Then
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    public void getWorldLorebookEntryById_whenFound_thenReturnDetails() {

        // Given
        var world = WorldFixture.publicWorld().build();
        world.addLorebookEntry("Lorebook", "Lorebook", "Lorebook");

        insert(world, World.class);

        var entry = world.getLorebook().getFirst();

        // When
        Optional<WorldLorebookEntryDetails> result = reader.getWorldLorebookEntryById(
                entry.getPublicId(), world.getPublicId());

        // Then
        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result.get().id()).isEqualTo(entry.getPublicId());
        assertThat(result.get().worldId()).isEqualTo(world.getPublicId());
        assertThat(result.get().name()).isEqualTo(entry.getName());
        assertThat(result.get().description()).isEqualTo(entry.getDescription());
        assertThat(result.get().regex()).isEqualTo(entry.getRegex());
        assertThat(result.get().creationDate()).isNotNull();
        assertThat(result.get().lastUpdateDate()).isNotNull();
    }
}
