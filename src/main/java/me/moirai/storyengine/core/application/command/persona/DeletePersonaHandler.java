package me.moirai.storyengine.core.application.command.persona;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.port.inbound.persona.DeletePersona;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;

@CommandHandler
public class DeletePersonaHandler extends AbstractCommandHandler<DeletePersona, Void> {

    private static final String PERSONA_NOT_FOUND = "Persona was not found";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "Persona ID cannot be null or empty";
    private static final String USER_NO_PERMISSION_IN_PERSONA = "User does not have permission to delete the persona";

    private final PersonaRepository repository;

    public DeletePersonaHandler(PersonaRepository repository) {

        this.repository = repository;
    }

    @Override
    public void validate(DeletePersona request) {

        if (request.personaId() == null) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public Void execute(DeletePersona request) {

        var persona = repository.findByPublicId(request.personaId())
                .orElseThrow(() -> new AssetNotFoundException(PERSONA_NOT_FOUND));

        // TODO move to authorization
        if (!persona.canUserWrite(request.requesterId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION_IN_PERSONA);
        }

        repository.deleteByPublicId(request.personaId());

        return null;
    }
}
