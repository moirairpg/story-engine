package me.moirai.storyengine.core.application.usecase.persona;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.port.inbound.persona.GetPersonaById;
import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;

@QueryHandler
public class GetPersonaByIdHandler extends AbstractQueryHandler<GetPersonaById, PersonaDetails> {

    private static final String PERSONA_NOT_FOUND = "Persona was not found";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "Persona ID cannot be null or empty";
    private static final String USER_NO_PERMISSION_IN_PERSONA = "User does not have permission to view the persona";

    private final PersonaRepository repository;

    public GetPersonaByIdHandler(PersonaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(GetPersonaById request) {

        if (request.personaId() == null) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public PersonaDetails execute(GetPersonaById query) {

        var persona = repository.findByPublicId(query.personaId())
                .orElseThrow(() -> new AssetNotFoundException(PERSONA_NOT_FOUND));

        // TODO externalize to authorizer
        if (!persona.canUserRead(query.requesterId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION_IN_PERSONA);
        }

        return mapResult(persona);
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
}
