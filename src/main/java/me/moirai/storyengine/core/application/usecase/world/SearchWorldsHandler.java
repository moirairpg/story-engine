package me.moirai.storyengine.core.application.usecase.world;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.application.usecase.world.request.SearchWorlds;
import me.moirai.storyengine.core.application.usecase.world.result.SearchWorldsResult;
import me.moirai.storyengine.core.domain.world.WorldRepository;

@UseCaseHandler
public class SearchWorldsHandler extends AbstractUseCaseHandler<SearchWorlds, SearchWorldsResult> {

    private final WorldRepository repository;

    public SearchWorldsHandler(WorldRepository repository) {
        this.repository = repository;
    }

    @Override
    public SearchWorldsResult execute(SearchWorlds query) {

        return repository.search(query);
    }
}
