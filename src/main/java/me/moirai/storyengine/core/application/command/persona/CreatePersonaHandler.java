package me.moirai.storyengine.core.application.command.persona;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.domain.Permissions;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.port.inbound.persona.CreatePersona;
import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import reactor.core.publisher.Mono;

@CommandHandler
public class CreatePersonaHandler extends AbstractCommandHandler<CreatePersona, Mono<PersonaDetails>> {

    private static final String PERSONA_FLAGGED_BY_MODERATION = "Persona flagged by moderation";

    private final TextModerationPort moderationPort;
    private final PersonaRepository repository;

    public CreatePersonaHandler(TextModerationPort moderationPort, PersonaRepository repository) {
        this.moderationPort = moderationPort;
        this.repository = repository;
    }

    @Override
    public Mono<PersonaDetails> execute(CreatePersona command) {

        return moderateContent(command.personality())
                .flatMap(__ -> moderateContent(command.name()))
                .map(__ -> {
                    var permissions = Permissions.builder()
                            .ownerId(command.requesterId())
                            .usersAllowedToRead(command.usersAllowedToRead())
                            .usersAllowedToWrite(command.usersAllowedToWrite())
                            .build();

                    var persona = Persona.builder()
                            .name(command.name())
                            .personality(command.personality())
                            .visibility(command.visibility())
                            .permissions(permissions)
                            .build();

                    return repository.save(persona);
                })
                .map(this::mapResult);
    }

    private PersonaDetails mapResult(Persona persona) {

        return new PersonaDetails(
                persona.getPublicId(),
                persona.getName(),
                persona.getPersonality(),
                persona.getVisibility(),
                persona.getOwnerId(),
                persona.getUsersAllowedToRead(),
                persona.getUsersAllowedToWrite(),
                persona.getCreationDate(),
                persona.getLastUpdateDate());
    }

    // TODO move to validation
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
