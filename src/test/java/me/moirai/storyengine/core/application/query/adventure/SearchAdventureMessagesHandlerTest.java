package me.moirai.storyengine.core.application.query.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import me.moirai.storyengine.core.port.inbound.adventure.MessageSummary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.dto.PaginatedResult;
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
        var query = new SearchAdventureMessages(adventureId, 1, 10);
        var expected = PaginatedResult.<MessageSummary>of(List.of(), 0L, 1, 10);

        when(reader.search(query)).thenReturn(expected);

        // when
        var result = handler.handle(query);

        // then
        assertThat(result).isEqualTo(expected);
    }
}
