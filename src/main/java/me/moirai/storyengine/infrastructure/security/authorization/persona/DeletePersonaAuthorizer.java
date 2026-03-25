package me.moirai.storyengine.infrastructure.security.authorization.persona;

import static me.moirai.storyengine.common.enums.Role.ADMIN;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;
import me.moirai.storyengine.core.port.inbound.AssetPermissionsData;
import me.moirai.storyengine.core.port.outbound.persona.PersonaAuthorizationReader;

@Component
public class DeletePersonaAuthorizer implements OperationAuthorizer {

    private final PersonaAuthorizationReader reader;

    public DeletePersonaAuthorizer(PersonaAuthorizationReader reader) {
        this.reader = reader;
    }

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.DELETE_PERSONA;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {

        var personaId = context.getFieldAsUuid("personaId");
        var principal = context.getPrincipal();

        var authData = reader.getAuthorizationData(personaId)
                .orElseThrow(() -> new AssetNotFoundException("Persona not found"));

        return canWrite(authData, principal);
    }

    private boolean canWrite(AssetPermissionsData authData, MoiraiPrincipal principal) {
        return authData.ownerId().equals(principal.publicId())
                || authData.writers().contains(principal.publicId())
                || principal.role() == ADMIN;
    }
}
