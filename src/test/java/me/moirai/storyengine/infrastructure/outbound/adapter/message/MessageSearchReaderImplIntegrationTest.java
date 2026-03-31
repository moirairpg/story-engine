package me.moirai.storyengine.infrastructure.outbound.adapter.message;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.message.Message;
import me.moirai.storyengine.core.domain.message.MessageFixture;
import me.moirai.storyengine.core.domain.message.MessageStatus;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureMessages;
import me.moirai.storyengine.core.port.outbound.message.MessageSearchReader;

public class MessageSearchReaderImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MessageSearchReader reader;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void shouldReturnEmptyResultWhenNoMessages() {

        // Given
        var persona = insert(PersonaFixture.publicPersona().build(), Persona.class);
        var world = insert(WorldFixture.publicWorld().build(), World.class);
        var adventure = AdventureFixture.privateMultiplayerAdventure()
                .worldId(world.getId())
                .personaId(persona.getId())
                .build();
        insert(adventure, Adventure.class);

        var query = new SearchAdventureMessages(adventure.getPublicId(), null, null);

        // When
        var result = reader.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.totalItems()).isEqualTo(0);
        assertThat(result.data()).isEmpty();
    }

    @Test
    public void shouldReturnPagedMessages() {

        // Given
        var persona = insert(PersonaFixture.publicPersona().build(), Persona.class);
        var world = insert(WorldFixture.publicWorld().build(), World.class);
        var adventure = AdventureFixture.privateMultiplayerAdventure()
                .worldId(world.getId())
                .personaId(persona.getId())
                .build();
        var insertedAdventure = insert(adventure, Adventure.class);

        var userMsg = MessageFixture.userMessage().adventureId(insertedAdventure.getId()).build();
        var assistantMsg = MessageFixture.assistantMessage().adventureId(insertedAdventure.getId()).build();
        insert(userMsg, Message.class);
        insert(assistantMsg, Message.class);

        var query = new SearchAdventureMessages(insertedAdventure.getPublicId(), 1, 10);

        // When
        var result = reader.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.totalItems()).isEqualTo(2);
        assertThat(result.data()).hasSize(2);
    }

    @Test
    public void shouldReturnBothActiveAndChronicledMessages() {

        // Given
        var persona = insert(PersonaFixture.publicPersona().build(), Persona.class);
        var world = insert(WorldFixture.publicWorld().build(), World.class);
        var adventure = AdventureFixture.privateMultiplayerAdventure()
                .worldId(world.getId())
                .personaId(persona.getId())
                .build();
        var insertedAdventure = insert(adventure, Adventure.class);

        var activeMsg = MessageFixture.userMessage().adventureId(insertedAdventure.getId()).build();
        var chronicledMsg = MessageFixture.assistantMessage().adventureId(insertedAdventure.getId()).status(MessageStatus.CHRONICLED).build();

        insert(activeMsg, Message.class);
        insert(chronicledMsg, Message.class);

        var query = new SearchAdventureMessages(insertedAdventure.getPublicId(), 1, 10);

        // When
        var result = reader.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.totalItems()).isEqualTo(2);
        assertThat(result.data()).hasSize(2);
        assertThat(result.data()).anyMatch(m -> m.status() == MessageStatus.ACTIVE);
        assertThat(result.data()).anyMatch(m -> m.status() == MessageStatus.CHRONICLED);
    }

    @Test
    public void shouldSortByCreationDateDescending() {

        // Given
        var persona = insert(PersonaFixture.publicPersona().build(), Persona.class);
        var world = insert(WorldFixture.publicWorld().build(), World.class);
        var adventure = AdventureFixture.privateMultiplayerAdventure()
                .worldId(world.getId())
                .personaId(persona.getId())
                .build();
        var insertedAdventure = insert(adventure, Adventure.class);

        var first = insert(MessageFixture.userMessage().adventureId(insertedAdventure.getId()).build(), Message.class);
        var second = insert(MessageFixture.assistantMessage().adventureId(insertedAdventure.getId()).build(), Message.class);

        var query = new SearchAdventureMessages(insertedAdventure.getPublicId(), 1, 10);

        // When
        var result = reader.search(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(2);
        assertThat(result.data().get(0).id()).isEqualTo(second.getPublicId());
        assertThat(result.data().get(1).id()).isEqualTo(first.getPublicId());
    }
}
