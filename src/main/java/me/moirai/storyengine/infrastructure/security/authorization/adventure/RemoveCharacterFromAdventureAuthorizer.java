package me.moirai.storyengine.infrastructure.security.authorization.adventure;

import java.util.UUID;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureAuthorizationReader;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterReader;

@Component
public class RemoveCharacterFromAdventureAuthorizer implements OperationAuthorizer {

    private final AdventureAuthorizationReader reader;
    private final PlayerCharacterReader playerCharacterReader;

    public RemoveCharacterFromAdventureAuthorizer(
            AdventureAuthorizationReader reader,
            PlayerCharacterReader playerCharacterReader) {

        this.reader = reader;
        this.playerCharacterReader = playerCharacterReader;
    }

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.REMOVE_CHARACTER_FROM_ADVENTURE;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {

        var principal = context.getPrincipal();

        if (principal.isAdmin()) {
            return true;
        }

        var adventureId = context.getFieldAsUuid("adventureId");
        var authData = reader.getAuthorizationData(adventureId);

        if (authData.isEmpty()) {
            return false;
        }

        if (canManage(authData.get(), principal)) {
            return true;
        }

        return ownsCharacter(context.getFieldAsUuid("playerCharacterId"), principal);
    }

    private boolean ownsCharacter(UUID playerCharacterId, MoiraiPrincipal principal) {
        return playerCharacterReader.getOwnerUsername(playerCharacterId)
                .map(ownerUsername -> ownerUsername.equals(principal.username()))
                .orElse(false);
    }
}
