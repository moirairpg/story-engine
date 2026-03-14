package me.moirai.storyengine.core.application.usecase.persona;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.application.usecase.persona.request.SearchPersonas;
import me.moirai.storyengine.core.application.usecase.persona.result.SearchPersonasResult;
import me.moirai.storyengine.core.domain.persona.PersonaRepository;

@UseCaseHandler
public class SearchPersonasHandler extends AbstractUseCaseHandler<SearchPersonas, SearchPersonasResult> {

    private final PersonaRepository repository;

    public SearchPersonasHandler(PersonaRepository repository) {
        this.repository = repository;
    }

    @Override
    public SearchPersonasResult execute(SearchPersonas query) {

        return repository.search(query);
    }
}
