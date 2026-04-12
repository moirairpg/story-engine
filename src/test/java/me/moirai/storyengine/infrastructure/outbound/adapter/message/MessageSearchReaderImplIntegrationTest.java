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

        // given
        var world = insert(WorldFixture.publicWorld().build(), World.class);
        var adventure = AdventureFixture.privateMultiplayerAdventure()
                .worldId(world.getPublicId())
                .build();

        insert(adventure, Adventure.class);

        var query = new SearchAdventureMessages(adventure.getPublicId(), null, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).isEmpty();
        assertThat(result.hasMore()).isFalse();
    }

    @Test
    public void shouldReturnHasMoreFalseWhenFewerMessagesThanSize() {

        // given
        var world = insert(WorldFixture.publicWorld().build(), World.class);
        var adventure = AdventureFixture.privateMultiplayerAdventure()
                .worldId(world.getPublicId())
                .build();
        var insertedAdventure = insert(adventure, Adventure.class);

        insert(MessageFixture.userMessage().adventureId(insertedAdventure.getId()).build(), Message.class);
        insert(MessageFixture.assistantMessage().adventureId(insertedAdventure.getId()).build(), Message.class);

        var query = new SearchAdventureMessages(insertedAdventure.getPublicId(), null, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(2);
        assertThat(result.hasMore()).isFalse();
    }

    @Test
    public void shouldReturnHasMoreTrueWhenExactlyAsManyMessagesAsSize() {

        // given
        var world = insert(WorldFixture.publicWorld().build(), World.class);
        var adventure = AdventureFixture.privateMultiplayerAdventure()
                .worldId(world.getPublicId())
                .build();
        var insertedAdventure = insert(adventure, Adventure.class);

        insert(MessageFixture.userMessage().adventureId(insertedAdventure.getId()).build(), Message.class);
        insert(MessageFixture.assistantMessage().adventureId(insertedAdventure.getId()).build(), Message.class);

        var query = new SearchAdventureMessages(insertedAdventure.getPublicId(), null, 2);

        // when
        var result = reader.search(query);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(2);
        assertThat(result.hasMore()).isTrue();
    }

    @Test
    public void shouldReturnHasMoreTrueAndLimitResultsWhenMoreMessagesThanSize() {

        // given
        var world = insert(WorldFixture.publicWorld().build(), World.class);
        var adventure = AdventureFixture.privateMultiplayerAdventure()
                .worldId(world.getPublicId())
                .build();
        var insertedAdventure = insert(adventure, Adventure.class);

        insert(MessageFixture.userMessage().adventureId(insertedAdventure.getId()).build(), Message.class);
        insert(MessageFixture.assistantMessage().adventureId(insertedAdventure.getId()).build(), Message.class);
        insert(MessageFixture.userMessage().adventureId(insertedAdventure.getId()).build(), Message.class);

        var query = new SearchAdventureMessages(insertedAdventure.getPublicId(), null, 2);

        // when
        var result = reader.search(query);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(2);
        assertThat(result.hasMore()).isTrue();
    }

    @Test
    public void shouldReturnOnlyMessagesOlderThanCursor() {

        // given
        var world = insert(WorldFixture.publicWorld().build(), World.class);
        var adventure = AdventureFixture.privateMultiplayerAdventure()
                .worldId(world.getPublicId())
                .build();
        var insertedAdventure = insert(adventure, Adventure.class);

        var first = insert(MessageFixture.userMessage().adventureId(insertedAdventure.getId()).build(), Message.class);
        var second = insert(MessageFixture.assistantMessage().adventureId(insertedAdventure.getId()).build(), Message.class);
        insert(MessageFixture.userMessage().adventureId(insertedAdventure.getId()).build(), Message.class);

        var query = new SearchAdventureMessages(insertedAdventure.getPublicId(), second.getPublicId(), 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).id()).isEqualTo(first.getPublicId());
        assertThat(result.hasMore()).isFalse();
    }

    @Test
    public void shouldReturnEmptyWhenCursorIsOlderThanAllMessages() {

        // given
        var world = insert(WorldFixture.publicWorld().build(), World.class);
        var adventure = AdventureFixture.privateMultiplayerAdventure()
                .worldId(world.getPublicId())
                .build();
        var insertedAdventure = insert(adventure, Adventure.class);

        var first = insert(MessageFixture.userMessage().adventureId(insertedAdventure.getId()).build(), Message.class);
        insert(MessageFixture.assistantMessage().adventureId(insertedAdventure.getId()).build(), Message.class);

        var query = new SearchAdventureMessages(insertedAdventure.getPublicId(), first.getPublicId(), 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).isEmpty();
        assertThat(result.hasMore()).isFalse();
    }

    @Test
    public void shouldReturnBothActiveAndChronicledMessages() {

        // given
        var world = insert(WorldFixture.publicWorld().build(), World.class);
        var adventure = AdventureFixture.privateMultiplayerAdventure()
                .worldId(world.getPublicId())
                .build();
        var insertedAdventure = insert(adventure, Adventure.class);

        var activeMsg = MessageFixture.userMessage().adventureId(insertedAdventure.getId()).build();
        var chronicledMsg = MessageFixture.assistantMessage().adventureId(insertedAdventure.getId()).status(MessageStatus.CHRONICLED).build();

        insert(activeMsg, Message.class);
        insert(chronicledMsg, Message.class);

        var query = new SearchAdventureMessages(insertedAdventure.getPublicId(), null, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(2);
        assertThat(result.data()).anyMatch(m -> m.status() == MessageStatus.ACTIVE);
        assertThat(result.data()).anyMatch(m -> m.status() == MessageStatus.CHRONICLED);
    }

    @Test
    public void shouldSortByPublicIdDescending() {

        // given
        var world = insert(WorldFixture.publicWorld().build(), World.class);
        var adventure = AdventureFixture.privateMultiplayerAdventure()
                .worldId(world.getPublicId())
                .build();
        var insertedAdventure = insert(adventure, Adventure.class);

        var first = insert(MessageFixture.userMessage().adventureId(insertedAdventure.getId()).build(), Message.class);
        var second = insert(MessageFixture.assistantMessage().adventureId(insertedAdventure.getId()).build(), Message.class);

        var query = new SearchAdventureMessages(insertedAdventure.getPublicId(), null, 10);

        // when
        var result = reader.search(query);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(2);
        assertThat(result.data().get(0).id()).isEqualTo(second.getPublicId());
        assertThat(result.data().get(1).id()).isEqualTo(first.getPublicId());
    }
}
