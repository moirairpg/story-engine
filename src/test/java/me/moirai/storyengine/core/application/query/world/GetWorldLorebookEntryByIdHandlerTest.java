package me.moirai.storyengine.core.application.query.world;

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

import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntryFixture;
import me.moirai.storyengine.core.port.inbound.world.GetWorldLorebookEntryById;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;
import me.moirai.storyengine.core.port.outbound.world.WorldLorebookReader;

@ExtendWith(MockitoExtension.class)
public class GetWorldLorebookEntryByIdHandlerTest {

    @Mock
    private WorldLorebookReader reader;

    @InjectMocks
    private GetWorldLorebookEntryByIdHandler handler;

    @Test
    public void errorWhenQueryIsNull() {

        // Given
        GetWorldLorebookEntryById query = null;

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void errorWhenEntryIdIsNull() {

        // Given
        var query = new GetWorldLorebookEntryById(null, WorldFixture.PUBLIC_ID);

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void errorWhenWorldIdIsNull() {

        // Given
        var query = new GetWorldLorebookEntryById(WorldLorebookEntryFixture.PUBLIC_ID, null);

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void getWorldLorebookEntryById_whenNotFound_thenThrowException() {

        // Given
        var query = new GetWorldLorebookEntryById(
                WorldLorebookEntryFixture.PUBLIC_ID, WorldFixture.PUBLIC_ID);

        when(reader.getWorldLorebookEntryById(any(UUID.class), any(UUID.class))).thenReturn(Optional.empty());

        // Then
        assertThrows(NotFoundException.class, () -> handler.handle(query));
    }

    @Test
    public void getWorldLorebookEntryById_whenFound_thenReturnDetails() {

        // Given
        var expectedDetails = new WorldLorebookEntryDetails(
                WorldLorebookEntryFixture.PUBLIC_ID, WorldFixture.PUBLIC_ID,
                "White River", "Description", null, null);

        var query = new GetWorldLorebookEntryById(
                WorldLorebookEntryFixture.PUBLIC_ID, WorldFixture.PUBLIC_ID);

        when(reader.getWorldLorebookEntryById(any(UUID.class), any(UUID.class)))
                .thenReturn(Optional.of(expectedDetails));

        // When
        WorldLorebookEntryDetails result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(WorldLorebookEntryFixture.PUBLIC_ID);
    }
}
