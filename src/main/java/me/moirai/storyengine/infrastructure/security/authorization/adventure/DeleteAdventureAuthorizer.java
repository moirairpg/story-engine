package me.moirai.storyengine.infrastructure.security.authorization.adventure;

import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

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

        return adventure.isOwner(principal.id()) || adventure.canWrite(principal.id());
    }
}
