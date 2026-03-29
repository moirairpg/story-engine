package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
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
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureLorebookReader;

public class AdventureLorebookReaderImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AdventureLorebookReader reader;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void getAdventureLorebookEntryById_whenNotFound_thenReturnEmpty() {

        // Given
        var entryPublicId = UUID.randomUUID();
        var adventurePublicId = UUID.randomUUID();

        // When
        var result = reader.getAdventureLorebookEntryById(entryPublicId, adventurePublicId);

        // Then
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    public void getAdventureLorebookEntryById_whenFound_thenReturnDetails() {

        // Given
        var persona = insert(PersonaFixture.publicPersona().build(), Persona.class);
        var world = insert(WorldFixture.publicWorld().build(), World.class);
        var adventure = AdventureFixture.privateMultiplayerAdventure()
                .worldId(world.getId())
                .personaId(persona.getId())
                .build();

        adventure.addLorebookEntry("Lorebook", "Lorebook", "Lorebook", null);

        insert(adventure, Adventure.class);

        var entry = adventure.getLorebook().getFirst();

        // When
        Optional<AdventureLorebookEntryDetails> result = reader.getAdventureLorebookEntryById(
                entry.getPublicId(), adventure.getPublicId());

        // Then
        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result.get().id()).isEqualTo(entry.getPublicId());
        assertThat(result.get().adventureId()).isEqualTo(adventure.getPublicId());
        assertThat(result.get().name()).isEqualTo(entry.getName());
        assertThat(result.get().description()).isEqualTo(entry.getDescription());
        assertThat(result.get().regex()).isEqualTo(entry.getRegex());
        assertThat(result.get().isPlayerCharacter()).isFalse();
        assertThat(result.get().creationDate()).isNotNull();
        assertThat(result.get().lastUpdateDate()).isNotNull();
    }

    @Test
    public void getAllByIds_whenListIsEmpty_thenReturnEmptyList() {

        // Given
        var emptyList = List.<UUID>of();

        // When
        var result = reader.getAllByIds(emptyList);

        // Then
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    public void getAllByIds_whenIdsExist_thenReturnMatchingEntries() {

        // Given
        var persona = insert(PersonaFixture.publicPersona().build(), Persona.class);
        var world = insert(WorldFixture.publicWorld().build(), World.class);
        var adventure = AdventureFixture.privateMultiplayerAdventure()
                .worldId(world.getId())
                .personaId(persona.getId())
                .build();

        adventure.addLorebookEntry("Entry One", "regex1", "Description One", null);
        adventure.addLorebookEntry("Entry Two", "regex2", "Description Two", null);

        insert(adventure, Adventure.class);

        var entryOne = adventure.getLorebook().get(0);
        var entryTwo = adventure.getLorebook().get(1);

        // When
        var result = reader.getAllByIds(List.of(entryOne.getPublicId(), entryTwo.getPublicId()));

        // Then
        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.stream().map(AdventureLorebookEntryDetails::id))
                .containsExactlyInAnyOrder(entryOne.getPublicId(), entryTwo.getPublicId());
    }

    @Test
    public void getAllByIds_whenSomeIdsDoNotExist_thenReturnOnlyMatching() {

        // Given
        var persona = insert(PersonaFixture.publicPersona().build(), Persona.class);
        var world = insert(WorldFixture.publicWorld().build(), World.class);
        var adventure = AdventureFixture.privateMultiplayerAdventure()
                .worldId(world.getId())
                .personaId(persona.getId())
                .build();

        adventure.addLorebookEntry("Existing Entry", "regex", "Description", null);

        insert(adventure, Adventure.class);

        var existingEntry = adventure.getLorebook().getFirst();
        var nonExistentId = UUID.randomUUID();

        // When
        var result = reader.getAllByIds(List.of(existingEntry.getPublicId(), nonExistentId));

        // Then
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(existingEntry.getPublicId());
    }
}
