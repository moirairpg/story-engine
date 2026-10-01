package me.moirai.storyengine.infrastructure.security.authorization.world;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;
import me.moirai.storyengine.core.port.inbound.AssetPermissionsData;
import me.moirai.storyengine.core.port.outbound.world.WorldAuthorizationReader;

@Component
public class ManageWorldPermissionsAuthorizer implements OperationAuthorizer {

    private final WorldAuthorizationReader reader;

    public ManageWorldPermissionsAuthorizer(WorldAuthorizationReader reader) {
        this.reader = reader;
    }

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.MANAGE_WORLD_PERMISSIONS;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {

        var worldId = context.getFieldAsUuid("worldId");
        var principal = context.getPrincipal();

        return reader.getAuthorizationData(worldId)
                .map(authData -> isOwner(authData, principal))
                .orElse(false);
    }

    private boolean isOwner(AssetPermissionsData authorizationData, MoiraiPrincipal principal) {
        return authorizationData.ownerId().equals(principal.publicId())
                || principal.isAdmin();
    }
}
