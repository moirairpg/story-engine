package me.moirai.storyengine.infrastructure.security.authorization.world;

import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

public class DeleteWorldAuthorizer implements OperationAuthorizer {

    private final WorldRepository worldRepository;

    public DeleteWorldAuthorizer(WorldRepository worldRepository) {
        this.worldRepository = worldRepository;
    }

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.DELETE_WORLD;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {

        var worldId = context.getFieldAsUuid("worldId");
        var principal = context.getPrincipal();

        var world = worldRepository.findByPublicId(worldId)
                .orElseThrow(() -> new AssetNotFoundException("World not found"));

        return world.canUserWrite(principal.discordId());
    }
}
