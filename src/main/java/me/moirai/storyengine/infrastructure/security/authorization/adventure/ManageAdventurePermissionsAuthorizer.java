package me.moirai.storyengine.infrastructure.security.authorization.adventure;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;
import me.moirai.storyengine.core.port.inbound.AssetPermissionsData;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureAuthorizationReader;

@Component
public class ManageAdventurePermissionsAuthorizer implements OperationAuthorizer {

    private final AdventureAuthorizationReader reader;

    public ManageAdventurePermissionsAuthorizer(AdventureAuthorizationReader reader) {
        this.reader = reader;
    }

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.MANAGE_ADVENTURE_PERMISSIONS;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {

        var adventureId = context.getFieldAsUuid("adventureId");
        var principal = context.getPrincipal();

        return reader.getAuthorizationData(adventureId)
                .map(authData -> isOwner(authData, principal))
                .orElse(false);
    }

    private boolean isOwner(AssetPermissionsData authorizationData, MoiraiPrincipal principal) {
        return authorizationData.ownerId().equals(principal.publicId())
                || principal.isAdmin();
    }
}
