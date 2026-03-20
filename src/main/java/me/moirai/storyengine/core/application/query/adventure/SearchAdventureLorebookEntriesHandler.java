package me.moirai.storyengine.core.application.query.adventure;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.core.port.inbound.LorebookEntrySummary;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntries;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureLorebookSearchReader;

@QueryHandler
public class SearchAdventureLorebookEntriesHandler
        extends AbstractQueryHandler<SearchAdventureLorebookEntries, PaginatedResult<LorebookEntrySummary>> {

    private final AdventureLorebookSearchReader reader;

    public SearchAdventureLorebookEntriesHandler(AdventureLorebookSearchReader reader) {
        this.reader = reader;
    }

    @Override
    public PaginatedResult<LorebookEntrySummary> execute(SearchAdventureLorebookEntries query) {

        return reader.search(query);
    }
}
