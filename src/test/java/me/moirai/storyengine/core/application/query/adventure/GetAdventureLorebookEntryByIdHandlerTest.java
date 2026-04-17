package me.moirai.storyengine.core.application.query.adventure;

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
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntryFixture;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureLorebookEntryById;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureLorebookReader;

@ExtendWith(MockitoExtension.class)
public class GetAdventureLorebookEntryByIdHandlerTest {

    @Mock
    private AdventureLorebookReader reader;

    @InjectMocks
    private GetAdventureLorebookEntryByIdHandler handler;

    @Test
    public void errorWhenQueryIsNull() {

        // Given
        GetAdventureLorebookEntryById query = null;

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void errorWhenEntryIdIsNull() {

        // Given
        var query = new GetAdventureLorebookEntryById(null, AdventureFixture.PUBLIC_ID);

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void errorWhenAdventureIdIsNull() {

        // Given
        var query = new GetAdventureLorebookEntryById(UUID.randomUUID(), null);

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void getAdventureLorebookEntryById_whenNotFound_thenThrowException() {

        // Given
        var query = new GetAdventureLorebookEntryById(
                AdventureLorebookEntryFixture.PUBLIC_ID, AdventureFixture.PUBLIC_ID);

        when(reader.getAdventureLorebookEntryById(any(UUID.class), any(UUID.class))).thenReturn(Optional.empty());

        // Then
        assertThrows(NotFoundException.class, () -> handler.handle(query));
    }

    @Test
    public void getAdventureLorebookEntryById_whenFound_thenReturnDetails() {

        // Given
        var expectedDetails = new AdventureLorebookEntryDetails(
                AdventureLorebookEntryFixture.PUBLIC_ID, AdventureFixture.PUBLIC_ID,
                "White River", "Description", null, false, null, null);

        var query = new GetAdventureLorebookEntryById(
                AdventureLorebookEntryFixture.PUBLIC_ID, AdventureFixture.PUBLIC_ID);

        when(reader.getAdventureLorebookEntryById(any(UUID.class), any(UUID.class)))
                .thenReturn(Optional.of(expectedDetails));

        // When
        AdventureLorebookEntryDetails result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(AdventureLorebookEntryFixture.PUBLIC_ID);
        assertThat(result.name()).isEqualTo(expectedDetails.name());
        assertThat(result.description()).isEqualTo(expectedDetails.description());
        assertThat(result.isPlayerCharacter()).isFalse();
    }
}
