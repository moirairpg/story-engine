package me.moirai.storyengine.infrastructure.inbound.rest.validation;

import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import me.moirai.storyengine.core.port.outbound.generation.TokenizerPort;

@Component
public class TokenCountValidator implements ConstraintValidator<TokenCount, String> {

    private final TokenizerPort tokenizerPort;
    private int min;
    private int max;

    public TokenCountValidator(TokenizerPort tokenizerPort) {
        this.tokenizerPort = tokenizerPort;
    }

    @Override
    public void initialize(TokenCount annotation) {
        this.min = annotation.min();
        this.max = annotation.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        var tokenCount = tokenizerPort.getTokenCountFrom(value);
        return tokenCount >= min && tokenCount <= max;
    }
}
