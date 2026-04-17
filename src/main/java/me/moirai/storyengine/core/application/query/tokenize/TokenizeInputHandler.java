package me.moirai.storyengine.core.application.query.tokenize;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.core.port.inbound.tokenize.TokenizeInput;
import me.moirai.storyengine.core.port.inbound.tokenize.TokenizeResult;
import me.moirai.storyengine.core.port.outbound.generation.TokenizerPort;

@QueryHandler
public class TokenizeInputHandler extends AbstractQueryHandler<TokenizeInput, TokenizeResult> {

    private final TokenizerPort tokenizerPort;

    public TokenizeInputHandler(TokenizerPort tokenizerPort) {
        this.tokenizerPort = tokenizerPort;
    }

    @Override
    public TokenizeResult execute(TokenizeInput useCase) {

        return tokenizerPort.tokenize(useCase.input());
    }
}
