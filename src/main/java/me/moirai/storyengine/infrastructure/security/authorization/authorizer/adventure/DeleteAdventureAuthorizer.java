package me.moirai.storyengine.infrastructure.security.authorization.authorizer.adventure;

import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.infrastructure.security.authorization.AuthorizationContext;
import me.moirai.storyengine.infrastructure.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.infrastructure.security.authorization.OperationAuthorizer;

public class DeleteAdventureAuthorizer implements OperationAuthorizer {

    private final AdventureRepository adventureRepository;

    public DeleteAdventureAuthorizer(AdventureRepository adventureRepository) {
        this.adventureRepository = adventureRepository;
    }

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.DELETE_ADVENTURE;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {

        var adventureId = context.getFieldAsUuid("adventureId");
        var principal = context.getPrincipal();

        var adventure = adventureRepository.findByPublicId(adventureId)
                .orElseThrow(() -> new AssetNotFoundException("Adventure not found"));

        return adventure.canUserWrite(principal.discordId());
    }
}
