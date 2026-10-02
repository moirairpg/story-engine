package me.moirai.storyengine.core.application.query.adventure;

import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.dto.CursorResult;
import me.moirai.storyengine.common.dto.MessageSummary;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureMessages;
import me.moirai.storyengine.core.port.outbound.message.MessageSearchReader;

@QueryHandler
@Authorize(operation = AuthorizationOperation.VIEW_ADVENTURE, fields = "#request.adventureId")
public class SearchAdventureMessagesHandler
        extends AbstractQueryHandler<SearchAdventureMessages, CursorResult<MessageSummary>> {

    private final MessageSearchReader reader;

    public SearchAdventureMessagesHandler(MessageSearchReader reader) {
        this.reader = reader;
    }

    @Override
    public CursorResult<MessageSummary> execute(SearchAdventureMessages query) {
        return reader.search(query);
    }
}
