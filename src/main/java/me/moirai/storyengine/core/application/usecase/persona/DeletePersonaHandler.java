package me.moirai.storyengine.core.application.usecase.persona;

import static org.apache.commons.lang3.StringUtils.isBlank;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.persona.DeletePersona;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.domain.persona.Persona;

@UseCaseHandler
public class DeletePersonaHandler extends AbstractUseCaseHandler<DeletePersona, Void> {

    private static final String PERSONA_NOT_FOUND = "Persona was not found";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "Persona ID cannot be null or empty";
    private static final String USER_NO_PERMISSION_IN_PERSONA = "User does not have permission to delete the persona";

    private final PersonaRepository repository;

    public DeletePersonaHandler(PersonaRepository repository) {

        this.repository = repository;
    }

    @Override
    public void validate(DeletePersona request) {

        if (isBlank(request.getId())) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public Void execute(DeletePersona request) {

        Persona persona = repository.findById(request.getId())
                .orElseThrow(() -> new AssetNotFoundException(PERSONA_NOT_FOUND));

        if (!persona.canUserWrite(request.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION_IN_PERSONA);
        }

        repository.deleteById(request.getId());

        return null;
    }
}
