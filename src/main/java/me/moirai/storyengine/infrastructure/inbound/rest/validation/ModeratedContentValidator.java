package me.moirai.storyengine.infrastructure.inbound.rest.validation;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;

@Component
public class ModeratedContentValidator implements ConstraintValidator<Moderated, String> {

    private static final String FLAGGED_BY_MODERATION = "Content flagged by moderation for %s";
    private static final Moderation DEFAULT_MODERATION = Moderation.PERMISSIVE;

    private final TextModerationPort textModerationPort;

    public ModeratedContentValidator(TextModerationPort textModerationPort) {
        this.textModerationPort = textModerationPort;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (StringUtils.isBlank(value)) {
            return true;
        }

        var flaggedTopics = getFlaggedTopics(value);
        if (CollectionUtils.isNotEmpty(flaggedTopics)) {
            var message = String.format(FLAGGED_BY_MODERATION, String.join(", ", flaggedTopics));

            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();

            return false;
        }

        return true;
    }

    private List<String> getFlaggedTopics(String input) {

        var response = textModerationPort.moderate(input);
        return response.moderationScores()
                .entrySet()
                .stream()
                .filter(this::isTopicFlagged)
                .map(Map.Entry::getKey)
                .toList();
    }

    private boolean isTopicFlagged(Entry<String, Double> entry) {
        return entry.getValue() > DEFAULT_MODERATION.getThresholds().get(entry.getKey());
    }
}
