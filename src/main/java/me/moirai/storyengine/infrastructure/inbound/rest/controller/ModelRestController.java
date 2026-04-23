package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import me.moirai.storyengine.common.cqs.query.QueryRunner;
import me.moirai.storyengine.common.web.SecurityContextAware;
import me.moirai.storyengine.core.port.inbound.model.AiModelResult;
import me.moirai.storyengine.core.port.inbound.model.SearchModels;

@RestController
@RequestMapping("/models")
@Tag(name = "AI Models", description = "Endpoints for managing MoirAI AI Models")
public class ModelRestController extends SecurityContextAware {

    private final QueryRunner queryRunner;

    public ModelRestController(QueryRunner queryRunner) {
        this.queryRunner = queryRunner;
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public List<AiModelResult> getAllAiModels(
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) String tokenLimit) {

        var query = new SearchModels(modelName, tokenLimit);
        return queryRunner.run(query);
    }
}
