package me.moirai.storyengine.core.application.query.persona;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.persona.GetPersonaById;
import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;
import me.moirai.storyengine.core.port.outbound.persona.PersonaReader;

@QueryHandler
public class GetPersonaByIdHandler extends AbstractQueryHandler<GetPersonaById, PersonaDetails> {

    private static final String PERSONA_NOT_FOUND = "Persona was not found";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "Persona ID cannot be null or empty";

    private final PersonaReader reader;

    public GetPersonaByIdHandler(PersonaReader reader) {
        this.reader = reader;
    }

    @Override
    public void validate(GetPersonaById request) {

        if (request.personaId() == null) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public PersonaDetails execute(GetPersonaById query) {

        return reader.getPersonaById(query.personaId())
                .orElseThrow(() -> new NotFoundException(PERSONA_NOT_FOUND));
    }
}
