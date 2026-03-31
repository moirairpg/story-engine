package me.moirai.storyengine.core.application.query.adventure;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.core.port.inbound.adventure.MessageSummary;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureMessages;
import me.moirai.storyengine.core.port.outbound.message.MessageSearchReader;

@QueryHandler
public class SearchAdventureMessagesHandler
        extends AbstractQueryHandler<SearchAdventureMessages, PaginatedResult<MessageSummary>> {

    private final MessageSearchReader reader;

    public SearchAdventureMessagesHandler(MessageSearchReader reader) {
        this.reader = reader;
    }

    @Override
    public PaginatedResult<MessageSummary> execute(SearchAdventureMessages query) {
        return reader.search(query);
    }
}
