package me.moirai.storyengine.core.application.usecase.persona;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonas;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonasResult;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;

@QueryHandler
public class SearchPersonasHandler extends AbstractQueryHandler<SearchPersonas, SearchPersonasResult> {

    private final PersonaRepository repository;

    public SearchPersonasHandler(PersonaRepository repository) {
        this.repository = repository;
    }

    @Override
    public SearchPersonasResult execute(SearchPersonas query) {

        return repository.search(query);
    }
}
