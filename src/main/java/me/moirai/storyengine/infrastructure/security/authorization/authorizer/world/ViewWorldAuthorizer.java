package me.moirai.storyengine.infrastructure.security.authorization.authorizer.world;

import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;
import me.moirai.storyengine.infrastructure.security.authorization.AuthorizationContext;
import me.moirai.storyengine.infrastructure.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.infrastructure.security.authorization.OperationAuthorizer;

public class ViewWorldAuthorizer implements OperationAuthorizer {

    private final WorldRepository worldRepository;

    public ViewWorldAuthorizer(WorldRepository worldRepository) {
        this.worldRepository = worldRepository;
    }

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.VIEW_WORLD;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {

        var worldId = context.getFieldAsUuid("worldId");
        var principal = context.getPrincipal();

        var world = worldRepository.findByPublicId(worldId)
                .orElseThrow(() -> new AssetNotFoundException("World not found"));

        return world.canUserRead(principal.discordId());
    }
}
