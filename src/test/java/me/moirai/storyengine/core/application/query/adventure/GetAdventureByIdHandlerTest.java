package me.moirai.storyengine.core.application.query.adventure;

import static me.moirai.storyengine.common.enums.Moderation.STRICT;
import static me.moirai.storyengine.common.enums.Visibility.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.ContextAttributesDto;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureById;
import me.moirai.storyengine.core.port.inbound.adventure.ModelConfigurationDto;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureReader;

@ExtendWith(MockitoExtension.class)
public class GetAdventureByIdHandlerTest {

    @Mock
    private AdventureReader reader;

    @InjectMocks
    private GetAdventureByIdHandler handler;

    @Test
    public void errorWhenIdIsNull() {

        // Given
        var query = new GetAdventureById(null);

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void errorWhenQueryIsNull() {

        // Given
        GetAdventureById query = null;

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void findAdventure_whenAdventureNotFound_thenThrowException() {

        // Given
        var query = new GetAdventureById(AdventureFixture.PUBLIC_ID);

        when(reader.getAdventureById(any(UUID.class))).thenReturn(Optional.empty());

        // Then
        assertThrows(AssetNotFoundException.class, () -> handler.handle(query));
    }

    @Test
    public void getAdventureById_whenFound_thenReturnDetails() {

        // Given
        var modelConfiguration = new ModelConfigurationDto(
                ArtificialIntelligenceModel.GPT4_MINI, 2048, 1.0, 0.0, 0.0, Set.of(), Map.of());

        var contextAttributes = new ContextAttributesDto(null, null, null, null, 0);

        var expectedDetails = new AdventureDetails(
                AdventureFixture.PUBLIC_ID,
                "Name",
                "desc",
                "start",
                WorldFixture.PUBLIC_ID,
                PersonaFixture.PUBLIC_ID,
                PRIVATE,
                STRICT,
                true,
                null,
                null,
                modelConfiguration,
                contextAttributes,
                Set.of());

        var query = new GetAdventureById(AdventureFixture.PUBLIC_ID);

        when(reader.getAdventureById(any(UUID.class))).thenReturn(Optional.of(expectedDetails));

        // When
        AdventureDetails result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(AdventureFixture.PUBLIC_ID);
        assertThat(result.worldId()).isEqualTo(WorldFixture.PUBLIC_ID);
        assertThat(result.personaId()).isEqualTo(PersonaFixture.PUBLIC_ID);
    }
}
