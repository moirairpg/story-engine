package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import me.moirai.storyengine.common.cqs.query.QueryRunner;
import me.moirai.storyengine.common.web.SecurityContextAware;
import me.moirai.storyengine.core.port.inbound.tokenize.TokenizeInput;
import me.moirai.storyengine.core.port.inbound.tokenize.TokenizeResult;

@RestController
@RequestMapping("/tokenize")
@Tag(name = "Tokenize", description = "Endpoints for MoirAI tokenization")
public class TokenizeRestController extends SecurityContextAware {

    private final QueryRunner queryRunner;

    public TokenizeRestController(QueryRunner queryRunner) {
        this.queryRunner = queryRunner;
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public TokenizeResult tokenize(@RequestParam(required = true) String input) {

        return queryRunner.run(new TokenizeInput(input));
    }
}
