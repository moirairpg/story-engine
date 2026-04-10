package me.moirai.storyengine.core.application.query.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.dto.CursorResult;
import me.moirai.storyengine.core.port.inbound.adventure.MessageSummary;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureMessages;
import me.moirai.storyengine.core.port.outbound.message.MessageSearchReader;

@ExtendWith(MockitoExtension.class)
public class SearchAdventureMessagesHandlerTest {

    @Mock
    private MessageSearchReader reader;

    private SearchAdventureMessagesHandler handler;

    @BeforeEach
    void setup() {
        handler = new SearchAdventureMessagesHandler(reader);
    }

    @Test
    public void shouldDelegateToReader() {

        // given
        var adventureId = UUID.randomUUID();
        var query = new SearchAdventureMessages(adventureId, null, 50);
        var expected = CursorResult.<MessageSummary>of(List.of(), 50);

        when(reader.search(query)).thenReturn(expected);

        // when
        var result = handler.handle(query);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void shouldReturnReaderResultUnchanged() {

        // given
        var adventureId = UUID.randomUUID();
        var lastMessageId = UUID.randomUUID();
        var query = new SearchAdventureMessages(adventureId, lastMessageId, 50);
        var expected = CursorResult.<MessageSummary>of(List.of(), 50);

        when(reader.search(query)).thenReturn(expected);

        // when
        var result = handler.handle(query);

        // then
        assertThat(result).isSameAs(expected);
    }
}
