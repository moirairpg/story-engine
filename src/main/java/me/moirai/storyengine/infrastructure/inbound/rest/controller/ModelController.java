package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import me.moirai.storyengine.common.usecases.UseCaseRunner;
import me.moirai.storyengine.common.web.SecurityContextAware;
import me.moirai.storyengine.core.port.inbound.model.AiModelResult;
import me.moirai.storyengine.core.port.inbound.model.SearchModels;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/model")
@Tag(name = "AI Models", description = "Endpoints for managing MoirAI AI Models")
public class ModelController extends SecurityContextAware {

    private final UseCaseRunner useCaseRunner;

    public ModelController(UseCaseRunner useCaseRunner) {
        this.useCaseRunner = useCaseRunner;
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<List<AiModelResult>> getAllAiModels(@RequestParam(required = false) String modelName, @RequestParam(required = false) String tokenLimit) {
        return mapWithAuthenticatedUser(authenticatedUser -> {
            SearchModels query = SearchModels.build(modelName, tokenLimit);
            return useCaseRunner.run(query);
        });
    }
}
