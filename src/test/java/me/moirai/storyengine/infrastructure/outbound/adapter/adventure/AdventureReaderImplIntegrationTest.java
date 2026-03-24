package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureReader;

public class AdventureReaderImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AdventureReader reader;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void getAdventureById_whenNotFound_thenReturnEmpty() {

        // Given
        var publicId = UUID.randomUUID();

        // When
        var result = reader.getAdventureById(publicId);

        // Then
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    public void getAdventureById_whenFound_thenReturnDetails() {

        // Given
        var persona = insert(PersonaFixture.publicPersona().build(), Persona.class);
        var world = insert(WorldFixture.publicWorld().build(), World.class);
        var adventure = AdventureFixture.privateMultiplayerAdventure()
                .worldId(world.getId())
                .personaId(persona.getId())
                .build();

        insert(adventure, Adventure.class);

        // When
        Optional<AdventureDetails> result = reader.getAdventureById(adventure.getPublicId());

        // Then
        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result.get().id()).isEqualTo(adventure.getPublicId());
        assertThat(result.get().name()).isEqualTo(adventure.getName());
        assertThat(result.get().worldId()).isEqualTo(world.getPublicId());
        assertThat(result.get().personaId()).isEqualTo(persona.getPublicId());
        assertThat(result.get().visibility()).isEqualTo(adventure.getVisibility().name());
        assertThat(result.get().isMultiplayer()).isTrue();
        assertThat(result.get().creationDate()).isNotNull();
        assertThat(result.get().lastUpdateDate()).isNotNull();

        assertThat(result.get().modelConfiguration()).isNotNull();
        assertThat(result.get().modelConfiguration().maxTokenLimit()).isEqualTo(100);
        assertThat(result.get().modelConfiguration().temperature()).isEqualTo(1.0);
        assertThat(result.get().modelConfiguration().frequencyPenalty()).isEqualTo(0.2);
        assertThat(result.get().modelConfiguration().presencePenalty()).isEqualTo(0.2);
        assertThat(result.get().modelConfiguration().stopSequences()).containsExactly("ABC");
        assertThat(result.get().modelConfiguration().logitBias()).hasSize(2)
                .containsEntry("ABC", 50.0)
                .containsEntry("DEF", 5.0);

        assertThat(result.get().contextAttributes()).isNotNull();
        assertThat(result.get().contextAttributes().nudge()).isEqualTo("Nudge");
        assertThat(result.get().contextAttributes().authorsNote()).isEqualTo("Author's note");
        assertThat(result.get().contextAttributes().remember()).isEqualTo("Remember");
        assertThat(result.get().contextAttributes().bump()).isEqualTo("Bump");
        assertThat(result.get().contextAttributes().bumpFrequency()).isEqualTo(1);
    }
}
