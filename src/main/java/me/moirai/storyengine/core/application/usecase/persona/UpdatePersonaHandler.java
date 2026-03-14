package me.moirai.storyengine.core.application.usecase.persona;

import static me.moirai.storyengine.common.domain.Visibility.PRIVATE;
import static me.moirai.storyengine.common.domain.Visibility.PUBLIC;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.persona.UpdatePersona;
import me.moirai.storyengine.core.port.inbound.persona.UpdatePersonaResult;
import me.moirai.storyengine.core.domain.adventure.Moderation;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaRepository;
import me.moirai.storyengine.core.port.TextModerationPort;
import reactor.core.publisher.Mono;

@UseCaseHandler
public class UpdatePersonaHandler extends AbstractUseCaseHandler<UpdatePersona, Mono<UpdatePersonaResult>> {

    private static final String PERSONA_NOT_FOUND = "Persona was not found";
    private static final String PERSONA_FLAGGED_BY_MODERATION = "Persona flagged by moderation";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "Persona ID cannot be null or empty";
    private static final String USER_NO_PERMISSION_IN_PERSONA = "User does not have permission to modify the persona";

    private final PersonaRepository repository;
    private final TextModerationPort moderationPort;

    public UpdatePersonaHandler(PersonaRepository repository,
            TextModerationPort moderationPort) {

        this.repository = repository;
        this.moderationPort = moderationPort;
    }

    @Override
    public void validate(UpdatePersona request) {

        if (isBlank(request.getId())) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public Mono<UpdatePersonaResult> execute(UpdatePersona request) {

        return moderateContent(request.getPersonality())
                .flatMap(__ -> moderateContent(request.getName()))
                .map(__ -> updatePersona(request))
                .map(personaUpdated -> UpdatePersonaResult.build(personaUpdated.getLastUpdateDate()));
    }

    private Persona updatePersona(UpdatePersona command) {

        Persona persona = repository.findById(command.getId())
                .orElseThrow(() -> new AssetNotFoundException(PERSONA_NOT_FOUND));

        if (!persona.canUserWrite(command.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION_IN_PERSONA);
        }

        if (isNotBlank(command.getName())) {
            persona.updateName(command.getName());
        }

        if (isNotBlank(command.getPersonality())) {
            persona.updatePersonality(command.getPersonality());
        }

        if (isNotBlank(command.getVisibility())) {
            if (command.getVisibility().equalsIgnoreCase(PUBLIC.name())) {
                persona.makePublic();
            } else if (command.getVisibility().equalsIgnoreCase(PRIVATE.name())) {
                persona.makePrivate();
            }
        }

        emptyIfNull(command.getUsersAllowedToReadToAdd())
                .forEach(persona::addReaderUser);

        emptyIfNull(command.getUsersAllowedToWriteToAdd())
                .forEach(persona::addWriterUser);

        emptyIfNull(command.getUsersAllowedToReadToRemove())
                .forEach(persona::removeReaderUser);

        emptyIfNull(command.getUsersAllowedToWriteToRemove())
                .forEach(persona::removeWriterUser);

        return repository.save(persona);
    }

    private Mono<List<String>> moderateContent(String personality) {

        if (isBlank(personality)) {
            return Mono.just(Collections.emptyList());
        }

        return getTopicsFlaggedByModeration(personality)
                .map(flaggedTopics -> {
                    if (isNotEmpty(flaggedTopics)) {
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
