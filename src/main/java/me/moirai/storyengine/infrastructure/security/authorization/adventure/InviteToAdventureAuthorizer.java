package me.moirai.storyengine.infrastructure.security.authorization.adventure;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;
import me.moirai.storyengine.core.port.inbound.AssetPermissionsData;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureAuthorizationReader;

@Component
public class InviteToAdventureAuthorizer implements OperationAuthorizer {

    private final AdventureAuthorizationReader reader;

    public InviteToAdventureAuthorizer(AdventureAuthorizationReader reader) {
        this.reader = reader;
    }

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.INVITE_TO_ADVENTURE;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {

        var principal = context.getPrincipal();

        if (principal.isAdmin()) {
            return true;
        }

        var adventureId = context.getFieldAsUuid("adventureId");

        return reader.getAuthorizationData(adventureId)
                .map(authData -> canInvite(authData, principal))
                .orElse(false);
    }

    private boolean canInvite(AssetPermissionsData authData, MoiraiPrincipal principal) {
        return authData.ownerId().equals(principal.publicId())
                || authData.writers().contains(principal.publicId());
    }
}
