package me.moirai.storyengine.core.domain.persona;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import me.moirai.storyengine.common.annotation.DomainService;
import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.core.application.usecase.persona.request.CreatePersona;
import me.moirai.storyengine.core.domain.Permissions;
import me.moirai.storyengine.core.domain.Visibility;
import me.moirai.storyengine.core.domain.adventure.Moderation;
import me.moirai.storyengine.core.port.TextModerationPort;
import reactor.core.publisher.Mono;

@DomainService
public class PersonaServiceImpl implements PersonaService {

    private static final String PERSONA_FLAGGED_BY_MODERATION = "Persona flagged by moderation";

    private final TextModerationPort moderationPort;
    private final PersonaRepository repository;

    public PersonaServiceImpl(TextModerationPort moderationPort, PersonaRepository repository) {
        this.moderationPort = moderationPort;
        this.repository = repository;
    }

    @Override
    public Mono<Persona> createFrom(CreatePersona command) {

        return moderateContent(command.getPersonality())
                .flatMap(__ -> moderateContent(command.getName()))
                .map(__ -> {
                    Persona.Builder personaBuilder = Persona.builder();
                    Permissions permissions = Permissions.builder()
                            .ownerId(command.getRequesterDiscordId())
                            .usersAllowedToRead(command.getUsersAllowedToRead())
                            .usersAllowedToWrite(command.getUsersAllowedToWrite())
                            .build();

                    Persona persona = personaBuilder.name(command.getName())
                            .personality(command.getPersonality())
                            .visibility(Visibility.fromString(command.getVisibility()))
                            .permissions(permissions)
                            .build();

                    return repository.save(persona);
                });
    }

    private Mono<List<String>> moderateContent(String personality) {

        if (StringUtils.isBlank(personality)) {
            return Mono.just(Collections.emptyList());
        }

        return getTopicsFlaggedByModeration(personality)
                .map(flaggedTopics -> {
                    if (CollectionUtils.isNotEmpty(flaggedTopics)) {
                        throw new ModerationException(PERSONA_FLAGGED_BY_MODERATION, flaggedTopics);
                    }

                    return flaggedTopics;
                });
    }

    private Mono<List<String>> getTopicsFlaggedByModeration(String input) {

        return moderationPort.moderate(input)
                .map(result -> result.getModerationScores()
                        .entrySet()
                        .stream()
                        .filter(this::isTopicFlagged)
                        .map(Map.Entry::getKey)
                        .toList());
    }

    private boolean isTopicFlagged(Entry<String, Double> entry) {
        return entry.getValue() > Moderation.PERMISSIVE.getThresholds().get(entry.getKey());
    }
}
