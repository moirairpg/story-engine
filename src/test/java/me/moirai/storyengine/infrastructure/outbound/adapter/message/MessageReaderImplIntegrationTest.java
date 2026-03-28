package me.moirai.storyengine.infrastructure.outbound.adapter.message;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.common.enums.AiRole;
import me.moirai.storyengine.core.domain.message.Message;
import me.moirai.storyengine.core.domain.message.MessageFixture;
import me.moirai.storyengine.core.port.outbound.message.MessageData;
import me.moirai.storyengine.core.port.outbound.message.MessageReader;

public class MessageReaderImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MessageReader reader;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void shouldReturnEmptyListWhenNoMessagesExist() {

        // Given
        var adventureId = 999L;

        // When
        var result = reader.findActiveByAdventureId(adventureId, 10);

        // Then
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    public void shouldReturnActiveMessagesInChronologicalOrderWhenMessagesExist() {

        // Given
        var userMessage = insert(MessageFixture.userMessage().build(), Message.class);
        var assistantMessage = insert(MessageFixture.assistantMessage().build(), Message.class);

        // When
        List<MessageData> result = reader.findActiveByAdventureId(1L, 10);

        // Then
        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.get(0).publicId()).isEqualTo(userMessage.getPublicId());
        assertThat(result.get(0).role()).isEqualTo(AiRole.USER);
        assertThat(result.get(1).publicId()).isEqualTo(assistantMessage.getPublicId());
        assertThat(result.get(1).role()).isEqualTo(AiRole.ASSISTANT);
    }

    @Test
    public void shouldRespectLimitWhenMoreMessagesThanLimitExist() {

        // Given
        insert(MessageFixture.userMessage().build(), Message.class);
        insert(MessageFixture.assistantMessage().build(), Message.class);
        insert(MessageFixture.userMessage().content("Second user message").build(), Message.class);

        // When
        List<MessageData> result = reader.findActiveByAdventureId(1L, 2);

        // Then
        assertThat(result).isNotNull().hasSize(2);
    }
}
