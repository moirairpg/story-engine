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
import me.moirai.storyengine.core.port.inbound.world.WorldDetails;
import me.moirai.storyengine.core.port.outbound.world.WorldReader;

public class WorldReaderImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WorldReader reader;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void getWorldById_whenWorldNotFound_thenReturnEmpty() {

        // Given
        var publicId = UUID.randomUUID();

        // When
        var result = reader.getWorldById(publicId);

        // Then
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    public void getWorldById_whenWorldFound_thenReturnDetails() {

        // Given
        var world = insert(WorldFixture.publicWorld().build(), World.class);

        // When
        Optional<WorldDetails> result = reader.getWorldById(world.getPublicId());

        // Then
        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result.get().id()).isEqualTo(world.getPublicId());
        assertThat(result.get().name()).isEqualTo(world.getName());
        assertThat(result.get().description()).isEqualTo(world.getDescription());
        assertThat(result.get().adventureStart()).isEqualTo(world.getAdventureStart());
        assertThat(result.get().creationDate()).isNotNull();
        assertThat(result.get().lastUpdateDate()).isNotNull();
    }
}
