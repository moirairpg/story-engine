package me.moirai.storyengine.core.application.query.adventure;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureSummary;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventures;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureSearchReader;

@QueryHandler
public class SearchAdventuresHandler extends AbstractQueryHandler<SearchAdventures, PaginatedResult<AdventureSummary>> {

    private final AdventureSearchReader reader;

    public SearchAdventuresHandler(AdventureSearchReader reader) {
        this.reader = reader;
    }

    @Override
    public PaginatedResult<AdventureSummary> execute(SearchAdventures query) {

        return reader.search(query);
    }
}
