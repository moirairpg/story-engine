package me.moirai.storyengine.core.application.usecase.world;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.application.usecase.world.request.CreateWorld;
import me.moirai.storyengine.core.application.usecase.world.result.CreateWorldResult;
import me.moirai.storyengine.core.domain.world.WorldService;
import reactor.core.publisher.Mono;

@UseCaseHandler
public class CreateWorldHandler extends AbstractUseCaseHandler<CreateWorld, Mono<CreateWorldResult>> {

    private final WorldService domainService;

    public CreateWorldHandler(WorldService domainService) {
        this.domainService = domainService;
    }

    @Override
    public Mono<CreateWorldResult> execute(CreateWorld command) {

        return domainService.createFrom(command)
                .map(worldCreated -> CreateWorldResult.build(worldCreated.getId()));
    }
}
