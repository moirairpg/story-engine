package me.moirai.storyengine.infrastructure.inbound.rest.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationResult;
import me.moirai.storyengine.infrastructure.inbound.rest.request.LorebookEntryModerationSource;

@Component
public class ModeratedLorebookValidator implements ConstraintValidator<ModeratedLorebook, Collection<? extends LorebookEntryModerationSource>> {

    private static final String FLAGGED_BY_MODERATION = "Lorebook entry '%s' flagged by moderation for: %s";
    private static final Moderation DEFAULT_MODERATION = Moderation.PERMISSIVE;

    private final TextModerationPort textModerationPort;
    private final boolean requestModerationEnabled;

    public ModeratedLorebookValidator(
            TextModerationPort textModerationPort,
            @Value("${moirai.config.request-moderation}") boolean requestModerationEnabled) {

        this.textModerationPort = textModerationPort;
        this.requestModerationEnabled = requestModerationEnabled;
    }

    @Override
    public boolean isValid(Collection<? extends LorebookEntryModerationSource> lorebook, ConstraintValidatorContext context) {

        if (!requestModerationEnabled || CollectionUtils.isEmpty(lorebook)) {
            return true;
        }

        var inputs = new ArrayList<Map.Entry<String, String>>();
        for (var entry : lorebook) {
            Stream.of(entry.name(), entry.description())
                    .filter(StringUtils::isNotBlank)
                    .forEach(text -> inputs.add(Map.entry(entry.name(), text)));
        }

        if (inputs.isEmpty()) {
            return true;
        }

        var texts = inputs.stream().map(Map.Entry::getValue).toArray(String[]::new);
        var results = textModerationPort.moderate(texts);

        var violations = new LinkedHashMap<String, List<String>>();
        for (var i = 0; i < results.size(); i++) {
            var result = results.get(i);
            if (result.isContentFlagged()) {
                var entryName = inputs.get(i).getKey();
                getFlaggedTopics(result).forEach(topic ->
                        violations.computeIfAbsent(entryName, k -> new ArrayList<>()).add(topic));
            }
        }

        if (!violations.isEmpty()) {
            context.disableDefaultConstraintViolation();
            violations.forEach((entryName, topics) -> {
                var message = String.format(FLAGGED_BY_MODERATION, entryName, String.join(", ", topics));
                context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            });

            return false;
        }

        return true;
    }

    private List<String> getFlaggedTopics(TextModerationResult result) {

        return result.moderationScores()
                .entrySet()
                .stream()
                .filter(this::isTopicFlagged)
                .map(Entry::getKey)
                .toList();
    }

    private boolean isTopicFlagged(Entry<String, Double> entry) {
        return entry.getValue() > DEFAULT_MODERATION.getThresholds().get(entry.getKey());
    }
}
