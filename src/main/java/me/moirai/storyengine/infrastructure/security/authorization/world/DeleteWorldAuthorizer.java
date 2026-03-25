package me.moirai.storyengine.infrastructure.security.authorization.world;

import static me.moirai.storyengine.common.enums.Role.ADMIN;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;
import me.moirai.storyengine.core.port.inbound.AssetPermissionsData;
import me.moirai.storyengine.core.port.outbound.world.WorldAuthorizationReader;

@Component
public class DeleteWorldAuthorizer implements OperationAuthorizer {

    private final WorldAuthorizationReader reader;

    public DeleteWorldAuthorizer(WorldAuthorizationReader reader) {
        this.reader = reader;
    }

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.DELETE_WORLD;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {

        var worldId = context.getFieldAsUuid("worldId");
        var principal = context.getPrincipal();

        var authData = reader.getAuthorizationData(worldId)
                .orElseThrow(() -> new AssetNotFoundException("World not found"));

        return canWrite(authData, principal);
    }

    private boolean canWrite(AssetPermissionsData authData, MoiraiPrincipal principal) {
        return authData.ownerId().equals(principal.publicId())
                || authData.writers().contains(principal.publicId())
                || principal.role() == ADMIN;
    }
}
