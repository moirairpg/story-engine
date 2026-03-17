package me.moirai.storyengine.core.application.query.adventure;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventures;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventuresResult;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@QueryHandler
public class SearchAdventuresHandler extends AbstractQueryHandler<SearchAdventures, SearchAdventuresResult> {

    private final AdventureRepository repository;

    public SearchAdventuresHandler(AdventureRepository repository) {
        this.repository = repository;
    }

    @Override
    public SearchAdventuresResult execute(SearchAdventures query) {

        return repository.search(query);
    }
}
