package me.moirai.storyengine.core.application.usecase.world;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.port.inbound.world.DeleteWorld;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@CommandHandler
public class DeleteWorldHandler extends AbstractCommandHandler<DeleteWorld, Void> {

    private static final String WORLD_TO_BE_VIEWED_WAS_NOT_FOUND = "World to be viewed was not found";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "World ID cannot be null or empty";
    private static final String USER_NO_PERMISSION_IN_PERSONA = "User does not have permission to delete the persona";

    private final WorldRepository repository;

    public DeleteWorldHandler(WorldRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(DeleteWorld command) {

        if (command.worldId() == null) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public Void execute(DeleteWorld command) {

        World world = repository.findByPublicId(command.worldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_TO_BE_VIEWED_WAS_NOT_FOUND));

        // TODO externalize to authorizer
        if (!world.canUserWrite(command.requesterId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION_IN_PERSONA);
        }

        repository.deleteByPublicId(command.worldId());

        return null;
    }
}
