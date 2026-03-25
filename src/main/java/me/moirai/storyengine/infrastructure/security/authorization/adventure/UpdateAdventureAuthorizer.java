package me.moirai.storyengine.infrastructure.security.authorization.adventure;

import static me.moirai.storyengine.common.enums.Role.ADMIN;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;
import me.moirai.storyengine.core.port.inbound.AssetPermissionsData;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureAuthorizationReader;

@Component
public class UpdateAdventureAuthorizer implements OperationAuthorizer {

    private final AdventureAuthorizationReader reader;

    public UpdateAdventureAuthorizer(AdventureAuthorizationReader reader) {
        this.reader = reader;
    }

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.UPDATE_ADVENTURE;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {

        var adventureId = context.getFieldAsUuid("adventureId");
        var principal = context.getPrincipal();

        var authData = reader.getAuthorizationData(adventureId)
                .orElseThrow(() -> new AssetNotFoundException("Adventure not found"));

        return canWrite(authData, principal);
    }

    private boolean canWrite(AssetPermissionsData authData, MoiraiPrincipal principal) {
        return authData.ownerId().equals(principal.publicId())
                || authData.writers().contains(principal.publicId())
                || principal.role() == ADMIN;
    }
}
