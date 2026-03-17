package me.moirai.storyengine.core.application.usecase.persona;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;
import me.moirai.storyengine.core.port.inbound.persona.UpdatePersona;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import reactor.core.publisher.Mono;

@CommandHandler
public class UpdatePersonaHandler extends AbstractCommandHandler<UpdatePersona, Mono<PersonaDetails>> {

    private static final String PERSONA_NOT_FOUND = "Persona was not found";
    private static final String PERSONA_FLAGGED_BY_MODERATION = "Persona flagged by moderation";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "Persona ID cannot be null or empty";
    private static final String USER_NO_PERMISSION_IN_PERSONA = "User does not have permission to modify the persona";

    private final PersonaRepository repository;
    private final TextModerationPort moderationPort;

    public UpdatePersonaHandler(
            PersonaRepository repository,
            TextModerationPort moderationPort) {

        this.repository = repository;
        this.moderationPort = moderationPort;
    }

    @Override
    public void validate(UpdatePersona request) {

        if (request.personaId() == null) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public Mono<PersonaDetails> execute(UpdatePersona request) {

        return moderateContent(request.personality())
                .flatMap(__ -> moderateContent(request.name()))
                .map(__ -> updatePersona(request))
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

    private Persona updatePersona(UpdatePersona command) {

        var persona = repository.findByPublicId(command.personaId())
                .orElseThrow(() -> new AssetNotFoundException(PERSONA_NOT_FOUND));

        // TODO move this to authorizer
        if (!persona.canUserWrite(command.requesterId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION_IN_PERSONA);
        }

        persona.updateName(command.name());
        persona.updatePersonality(command.personality());

        if (command.visibility() != null) {
            switch (command.visibility()) {
                case PUBLIC -> persona.makePublic();
                case PRIVATE -> persona.makePrivate();
                default -> persona.makePrivate();
            }
        }

        emptyIfNull(command.usersAllowedToReadToAdd())
                .forEach(persona::addReaderUser);

        emptyIfNull(command.usersAllowedToWriteToAdd())
                .forEach(persona::addWriterUser);

        emptyIfNull(command.usersAllowedToReadToRemove())
                .forEach(persona::removeReaderUser);

        emptyIfNull(command.usersAllowedToWriteToRemove())
                .forEach(persona::removeWriterUser);

        return repository.save(persona);
    }

    // TODO pass this to validation
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
