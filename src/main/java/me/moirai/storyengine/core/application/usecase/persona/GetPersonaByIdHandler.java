package me.moirai.storyengine.core.application.usecase.persona;

import static org.apache.commons.lang3.StringUtils.isBlank;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.persona.GetPersonaById;
import me.moirai.storyengine.core.port.inbound.persona.GetPersonaResult;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.domain.persona.Persona;

@UseCaseHandler
public class GetPersonaByIdHandler extends AbstractUseCaseHandler<GetPersonaById, GetPersonaResult> {

    private static final String PERSONA_NOT_FOUND = "Persona was not found";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "Persona ID cannot be null or empty";
    private static final String USER_NO_PERMISSION_IN_PERSONA = "User does not have permission to view the persona";

    private final PersonaRepository repository;

    public GetPersonaByIdHandler(PersonaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(GetPersonaById request) {

        if (isBlank(request.getId())) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public GetPersonaResult execute(GetPersonaById query) {

        Persona persona = repository.findById(query.getId())
                .orElseThrow(() -> new AssetNotFoundException(PERSONA_NOT_FOUND));

        if (!persona.canUserRead(query.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION_IN_PERSONA);
        }

        return mapResult(persona);
    }

    private GetPersonaResult mapResult(Persona persona) {

        return GetPersonaResult.builder()
                .id(persona.getId())
                .name(persona.getName())
                .personality(persona.getName())
                .visibility(persona.getVisibility().name())
                .creationDate(persona.getCreationDate())
                .lastUpdateDate(persona.getLastUpdateDate())
                .ownerId(persona.getOwnerId())
                .usersAllowedToRead(persona.getUsersAllowedToRead())
                .usersAllowedToWrite(persona.getUsersAllowedToWrite())
                .build();
    }
}
