package me.moirai.storyengine.infrastructure.outbound.adapter.world;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntries;
import me.moirai.storyengine.core.port.outbound.world.WorldLorebookSearchReader;

public class WorldLorebookSearchReaderImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WorldLorebookSearchReader reader;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void search_whenEntriesExist_thenReturnResults() {

        // Given
        var world = WorldFixture.publicWorld().build();
        world.addLorebookEntry("White River", "[Ww]hite [Rr]iver", "A famous river");
        insert(world, World.class);

        var query = new SearchWorldLorebookEntries(world.getPublicId(), null, null, null, null, null, "requesterId");

        // When
        var result = reader.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).isEqualTo(1);
        assertThat(result.data()).hasSize(1);
    }

    @Test
    public void search_whenNoEntries_thenReturnEmpty() {

        // Given
        var world = WorldFixture.publicWorld().build();
        insert(world, World.class);

        var query = new SearchWorldLorebookEntries(world.getPublicId(), null, null, null, null, null, "requesterId");

        // When
        var result = reader.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).isEqualTo(0);
        assertThat(result.data()).isEmpty();
    }

    @Test
    public void search_whenFilterByName_thenReturnMatchingResults() {

        // Given
        var world = WorldFixture.publicWorld().build();
        world.addLorebookEntry("White River", "[Ww]hite [Rr]iver", "A famous river");
        world.addLorebookEntry("Volin Habar", "[Vv]olin", "A warrior");
        insert(world, World.class);

        var query = new SearchWorldLorebookEntries(world.getPublicId(), "White", null, null, null, null, "requesterId");

        // When
        var result = reader.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).isEqualTo(1);
        assertThat(result.data().get(0).name()).isEqualTo("White River");
    }
}
