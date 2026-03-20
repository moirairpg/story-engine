package me.moirai.storyengine.core.application.query.world;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.core.port.inbound.LorebookEntrySummary;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntries;
import me.moirai.storyengine.core.port.outbound.world.WorldLorebookSearchReader;

@QueryHandler
public class SearchWorldLorebookEntriesHandler
        extends AbstractQueryHandler<SearchWorldLorebookEntries, PaginatedResult<LorebookEntrySummary>> {

    private final WorldLorebookSearchReader reader;

    public SearchWorldLorebookEntriesHandler(WorldLorebookSearchReader reader) {
        this.reader = reader;
    }

    @Override
    public PaginatedResult<LorebookEntrySummary> execute(SearchWorldLorebookEntries query) {

        return reader.search(query);
    }
}
