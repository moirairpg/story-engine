package me.moirai.storyengine.core.application.usecase.world;

import org.apache.commons.lang3.StringUtils;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.port.inbound.world.DeleteWorldLorebookEntry;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@UseCaseHandler
public class DeleteWorldLorebookEntryHandler extends AbstractUseCaseHandler<DeleteWorldLorebookEntry, Void> {

    private static final String ENTRY_ID_CANNOT_BE_NULL_OR_EMPTY = "Lorebook entry ID cannot be null or empty";
    private static final String WORLD_ID_CANNOT_BE_NULL_OR_EMPTY = "World ID cannot be null or empty";
    private static final String WORLD_TO_BE_UPDATED_WAS_NOT_FOUND = "World to be updated was not found";
    private static final String USER_DOES_NOT_HAVE_PERMISSION_TO_MODIFY_THIS_WORLD = "User does not have permission to modify this world";

    private final WorldRepository repository;

    public DeleteWorldLorebookEntryHandler(WorldRepository repository) {

        this.repository = repository;
    }

    @Override
    public void validate(DeleteWorldLorebookEntry command) {

        if (StringUtils.isBlank(command.getLorebookEntryId())) {
            throw new IllegalArgumentException(ENTRY_ID_CANNOT_BE_NULL_OR_EMPTY);
        }

        if (StringUtils.isBlank(command.getWorldId())) {
            throw new IllegalArgumentException(WORLD_ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public Void execute(DeleteWorldLorebookEntry command) {

        World world = repository.findById(command.getWorldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_TO_BE_UPDATED_WAS_NOT_FOUND));

        if (!world.canUserWrite(command.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_DOES_NOT_HAVE_PERMISSION_TO_MODIFY_THIS_WORLD);
        }

        world.removeLorebookEntry(command.getLorebookEntryId());
        repository.save(world);

        return null;
    }
}
