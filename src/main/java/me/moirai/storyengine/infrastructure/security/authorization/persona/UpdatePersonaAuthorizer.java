package me.moirai.storyengine.infrastructure.security.authorization.persona;

import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;

public class UpdatePersonaAuthorizer implements OperationAuthorizer {

    private final PersonaRepository personaRepository;

    public UpdatePersonaAuthorizer(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.UPDATE_PERSONA;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {

        var personaId = context.getFieldAsUuid("personaId");
        var principal = context.getPrincipal();

        var persona = personaRepository.findByPublicId(personaId)
                .orElseThrow(() -> new AssetNotFoundException("Persona not found"));

        return persona.canUserWrite(principal.discordId());
    }
}
