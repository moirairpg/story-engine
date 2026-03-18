package me.moirai.storyengine.infrastructure.inbound.rest.validation;

import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;

@Component
public class ModeratedContentValidator implements ConstraintValidator<Moderated, String> {

    private final TextModerationPort textModerationPort;

    public ModeratedContentValidator(TextModerationPort textModerationPort) {
        this.textModerationPort = textModerationPort;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        var result = textModerationPort.moderate(value).block();
        if (result.isContentFlagged()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "content flagged for: " + String.join(", ", result.getFlaggedTopics()))
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
