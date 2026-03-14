package me.moirai.storyengine.core.application.usecase.world;

import static org.apache.commons.lang3.StringUtils.isBlank;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.world.DeleteWorld;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;
import me.moirai.storyengine.core.domain.world.World;

@UseCaseHandler
public class DeleteWorldHandler extends AbstractUseCaseHandler<DeleteWorld, Void> {

    private static final String WORLD_TO_BE_VIEWED_WAS_NOT_FOUND = "World to be viewed was not found";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "World ID cannot be null or empty";
    private static final String USER_NO_PERMISSION_IN_PERSONA = "User does not have permission to delete the persona";

    private final WorldRepository repository;

    public DeleteWorldHandler(WorldRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(DeleteWorld command) {

        if (isBlank(command.getId())) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public Void execute(DeleteWorld command) {

        World world = repository.findById(command.getId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_TO_BE_VIEWED_WAS_NOT_FOUND));

        if (!world.canUserWrite(command.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION_IN_PERSONA);
        }

        repository.deleteById(command.getId());

        return null;
    }
}
