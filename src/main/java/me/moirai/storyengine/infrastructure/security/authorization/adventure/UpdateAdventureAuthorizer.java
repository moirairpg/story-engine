package me.moirai.storyengine.infrastructure.security.authorization.adventure;

import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

public class UpdateAdventureAuthorizer implements OperationAuthorizer {

    private final AdventureRepository adventureRepository;

    public UpdateAdventureAuthorizer(AdventureRepository adventureRepository) {
        this.adventureRepository = adventureRepository;
    }

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.UPDATE_ADVENTURE;
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
