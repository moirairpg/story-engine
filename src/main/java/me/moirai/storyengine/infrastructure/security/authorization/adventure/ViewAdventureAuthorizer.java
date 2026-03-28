package me.moirai.storyengine.infrastructure.security.authorization.adventure;

import static me.moirai.storyengine.common.enums.Role.ADMIN;
import static me.moirai.storyengine.common.enums.Visibility.PUBLIC;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;
import me.moirai.storyengine.core.port.inbound.AssetPermissionsData;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureAuthorizationReader;

@Component
public class ViewAdventureAuthorizer implements OperationAuthorizer {

    private final AdventureAuthorizationReader reader;

    public ViewAdventureAuthorizer(AdventureAuthorizationReader reader) {
        this.reader = reader;
    }

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.VIEW_ADVENTURE;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {

        var adventureId = context.getFieldAsUuid("adventureId");
        var principal = context.getPrincipal();

        var authData = reader.getAuthorizationData(adventureId)
                .orElseThrow(() -> new NotFoundException("Adventure not found"));

        return canRead(authData, principal);
    }

    private boolean canRead(AssetPermissionsData authData, MoiraiPrincipal principal) {
        return authData.visibility() == PUBLIC
                || authData.ownerId().equals(principal.publicId())
                || authData.readers().contains(principal.publicId())
                || authData.writers().contains(principal.publicId())
                || principal.role() == ADMIN;
    }
}
