package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.ChronicleSegment;
import me.moirai.storyengine.core.domain.chronicle.ChronicleSegmentFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.outbound.adventure.ChronicleSegmentReader;

public class ChronicleSegmentReaderImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ChronicleSegmentReader reader;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void shouldReturnEmptyListFromGetAllByIdsWhenNoSegmentsExist() {

        // Given
        var id = UUID.randomUUID();

        // When
        var result = reader.getAllByIds(List.of(id));

        // Then
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    public void shouldReturnEmptyListFromGetAllOrderedWhenNoSegmentsExist() {

        // Given
        var adventurePublicId = UUID.randomUUID();

        // When
        var result = reader.getAllOrdered(adventurePublicId);

        // Then
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    public void shouldReturnSegmentsByIds() {

        // Given
        var adventure = AdventureFixture.publicSingleplayerAdventure().build();
        insert(adventure, Adventure.class);

        var segment = insert(ChronicleSegmentFixture.chronicleSegment()
                .adventureId(adventure.getId())
                .build(), ChronicleSegment.class);

        // When
        var result = reader.getAllByIds(List.of(segment.getPublicId()));

        // Then
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).publicId()).isEqualTo(segment.getPublicId());
        assertThat(result.get(0).content()).isEqualTo(segment.getContent());
    }

    @Test
    public void shouldReturnEmptyListForUnknownIds() {

        // Given
        var adventure = AdventureFixture.publicSingleplayerAdventure().build();
        insert(adventure, Adventure.class);

        var chronicle = ChronicleSegmentFixture.chronicleSegment()
                .adventureId(adventure.getId())
                .build();

        insert(chronicle, ChronicleSegment.class);
        var unknownId = UUID.randomUUID();

        // When
        var result = reader.getAllByIds(List.of(unknownId));

        // Then
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    public void shouldReturnAllSegmentsOrderedByCreationDateAscending() {

        // Given
        var world = insert(WorldFixture.publicWorld().build(), World.class);
        var adventure = AdventureFixture.privateMultiplayerAdventure()
                .worldId(world.getPublicId())
                .build();
        var insertedAdventure = insert(adventure, Adventure.class);

        var first = insert(ChronicleSegmentFixture.chronicleSegment().adventureId(insertedAdventure.getId())
                .content("First event").build(), ChronicleSegment.class);
        var second = insert(ChronicleSegmentFixture.chronicleSegment().adventureId(insertedAdventure.getId())
                .content("Second event").build(), ChronicleSegment.class);

        // When
        var result = reader.getAllOrdered(insertedAdventure.getPublicId());

        // Then
        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.get(0).publicId()).isEqualTo(first.getPublicId());
        assertThat(result.get(1).publicId()).isEqualTo(second.getPublicId());
    }
}
