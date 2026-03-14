package me.moirai.storyengine.core.application.usecase.persona;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.domain.Permissions;
import me.moirai.storyengine.common.domain.Visibility;
import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.adventure.Moderation;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.port.inbound.persona.CreatePersona;
import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import reactor.core.publisher.Mono;

@UseCaseHandler
public class CreatePersonaHandler extends AbstractUseCaseHandler<CreatePersona, Mono<PersonaDetails>> {

    private static final String PERSONA_FLAGGED_BY_MODERATION = "Persona flagged by moderation";

    private final TextModerationPort moderationPort;
    private final PersonaRepository repository;

    public CreatePersonaHandler(TextModerationPort moderationPort, PersonaRepository repository) {
        this.moderationPort = moderationPort;
        this.repository = repository;
    }

    @Override
    public Mono<PersonaDetails> execute(CreatePersona command) {

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
                })
                .map(this::mapResult);
    }

    private PersonaDetails mapResult(Persona persona) {

        return PersonaDetails.builder()
                .id(persona.getId())
                .name(persona.getName())
                .personality(persona.getPersonality())
                .visibility(persona.getVisibility().name())
                .creationDate(persona.getCreationDate())
                .lastUpdateDate(persona.getLastUpdateDate())
                .ownerId(persona.getOwnerId())
                .usersAllowedToRead(persona.getUsersAllowedToRead())
                .usersAllowedToWrite(persona.getUsersAllowedToWrite())
                .build();
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
