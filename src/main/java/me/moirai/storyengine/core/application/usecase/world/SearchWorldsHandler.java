package me.moirai.storyengine.core.application.usecase.world;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.core.port.inbound.world.SearchWorlds;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldsResult;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@QueryHandler
public class SearchWorldsHandler extends AbstractQueryHandler<SearchWorlds, SearchWorldsResult> {

    private final WorldRepository repository;

    public SearchWorldsHandler(WorldRepository repository) {
        this.repository = repository;
    }

    @Override
    public SearchWorldsResult execute(SearchWorlds query) {

        return repository.search(query);
    }
}
