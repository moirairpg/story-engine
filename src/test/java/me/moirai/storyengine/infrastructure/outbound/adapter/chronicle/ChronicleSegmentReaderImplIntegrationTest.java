package me.moirai.storyengine.infrastructure.outbound.adapter.chronicle;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.core.domain.chronicle.ChronicleSegment;
import me.moirai.storyengine.core.domain.chronicle.ChronicleSegmentFixture;
import me.moirai.storyengine.core.port.outbound.chronicle.ChronicleSegmentReader;

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
        var adventureId = 999L;

        // When
        var result = reader.getAllOrdered(adventureId);

        // Then
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    public void shouldReturnSegmentsByIds() {

        // Given
        var segment = insert(ChronicleSegmentFixture.chronicleSegment().build(), ChronicleSegment.class);

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
        insert(ChronicleSegmentFixture.chronicleSegment().build(), ChronicleSegment.class);
        var unknownId = UUID.randomUUID();

        // When
        var result = reader.getAllByIds(List.of(unknownId));

        // Then
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    public void shouldReturnAllSegmentsOrderedByCreationDateAscending() {

        // Given
        var first = insert(ChronicleSegmentFixture.chronicleSegment().content("First event").build(), ChronicleSegment.class);
        var second = insert(ChronicleSegmentFixture.chronicleSegment().content("Second event").build(), ChronicleSegment.class);

        // When
        var result = reader.getAllOrdered(1L);

        // Then
        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.get(0).publicId()).isEqualTo(first.getPublicId());
        assertThat(result.get(1).publicId()).isEqualTo(second.getPublicId());
    }
}
