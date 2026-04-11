package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import static org.assertj.core.api.Assertions.assertThat;

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
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntries;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureLorebookSearchReader;

public class AdventureLorebookSearchReaderImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AdventureLorebookSearchReader reader;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void search_whenEntriesExist_thenReturnResults() {

        // Given
        var persona = insert(PersonaFixture.publicPersona().build(), Persona.class);
        var world = insert(WorldFixture.publicWorld().build(), World.class);
        var adventure = AdventureFixture.privateMultiplayerAdventure()
                .worldId(world.getPublicId())
                .personaId(persona.getId())
                .build();
        adventure.addLorebookEntry("White River", "A famous river", null);
        insert(adventure, Adventure.class);

        var query = new SearchAdventureLorebookEntries(adventure.getPublicId(), null, null, null, null, null);

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
        var persona = insert(PersonaFixture.publicPersona().build(), Persona.class);
        var world = insert(WorldFixture.publicWorld().build(), World.class);
        var adventure = AdventureFixture.privateMultiplayerAdventure()
                .worldId(world.getPublicId())
                .personaId(persona.getId())
                .build();
        insert(adventure, Adventure.class);

        var query = new SearchAdventureLorebookEntries(adventure.getPublicId(), null, null, null, null, null);

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
        var persona = insert(PersonaFixture.publicPersona().build(), Persona.class);
        var world = insert(WorldFixture.publicWorld().build(), World.class);
        var adventure = AdventureFixture.privateMultiplayerAdventure()
                .worldId(world.getPublicId())
                .personaId(persona.getId())
                .build();
        adventure.addLorebookEntry("White River", "A famous river", null);
        adventure.addLorebookEntry("Volin Habar", "A warrior", null);
        insert(adventure, Adventure.class);

        var query = new SearchAdventureLorebookEntries(adventure.getPublicId(), "White", null, null, null, null);

        // When
        var result = reader.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).isEqualTo(1);
        assertThat(result.data().get(0).name()).isEqualTo("White River");
    }
}
