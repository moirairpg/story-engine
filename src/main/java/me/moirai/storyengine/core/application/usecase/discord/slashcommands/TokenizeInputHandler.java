package me.moirai.storyengine.core.application.usecase.discord.slashcommands;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.port.TokenizerPort;

@UseCaseHandler
public class TokenizeInputHandler extends AbstractUseCaseHandler<TokenizeInput, Optional<TokenizeResult>> {

    private static final Logger LOG = LoggerFactory.getLogger(TokenizeInputHandler.class);

    private final TokenizerPort tokenizerPort;

    public TokenizeInputHandler(TokenizerPort tokenizerPort) {
        this.tokenizerPort = tokenizerPort;
    }

    @Override
    public Optional<TokenizeResult> execute(TokenizeInput useCase) {

        try {
            return Optional.of(tokenizerPort.tokenize(useCase.getInput()));
        } catch (Exception e) {
            LOG.error("Error tokenizing input -> {}", useCase.getInput(), e);
            return Optional.empty();
        }
    }
}
