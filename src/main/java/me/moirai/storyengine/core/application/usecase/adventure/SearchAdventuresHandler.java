package me.moirai.storyengine.core.application.usecase.adventure;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventures;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventuresResult;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@UseCaseHandler
public class SearchAdventuresHandler extends AbstractUseCaseHandler<SearchAdventures, SearchAdventuresResult> {

    private final AdventureRepository repository;

    public SearchAdventuresHandler(AdventureRepository repository) {
        this.repository = repository;
    }

    @Override
    public SearchAdventuresResult execute(SearchAdventures query) {

        return repository.search(query);
    }
}
