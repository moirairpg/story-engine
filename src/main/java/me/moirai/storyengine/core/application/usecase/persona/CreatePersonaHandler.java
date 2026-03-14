package me.moirai.storyengine.core.application.usecase.persona;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.application.usecase.persona.request.CreatePersona;
import me.moirai.storyengine.core.application.usecase.persona.result.CreatePersonaResult;
import me.moirai.storyengine.core.domain.persona.PersonaService;
import reactor.core.publisher.Mono;

@UseCaseHandler
public class CreatePersonaHandler extends AbstractUseCaseHandler<CreatePersona, Mono<CreatePersonaResult>> {

    private final PersonaService domainService;

    public CreatePersonaHandler(PersonaService domainService) {
        this.domainService = domainService;
    }

    @Override
    public Mono<CreatePersonaResult> execute(CreatePersona command) {

        return domainService.createFrom(command)
                .map(personaCreated -> CreatePersonaResult.build(personaCreated.getId()));
    }
}
