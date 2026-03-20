package me.moirai.storyengine.core.application.query.world;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.core.port.inbound.world.SearchWorlds;
import me.moirai.storyengine.core.port.inbound.world.WorldSummary;
import me.moirai.storyengine.core.port.outbound.world.WorldSearchReader;

@QueryHandler
public class SearchWorldsHandler extends AbstractQueryHandler<SearchWorlds, PaginatedResult<WorldSummary>> {

    private final WorldSearchReader reader;

    public SearchWorldsHandler(WorldSearchReader reader) {
        this.reader = reader;
    }

    @Override
    public PaginatedResult<WorldSummary> execute(SearchWorlds query) {

        return reader.search(query);
    }
}
