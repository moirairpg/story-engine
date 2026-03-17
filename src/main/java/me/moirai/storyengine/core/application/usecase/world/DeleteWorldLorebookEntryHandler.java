package me.moirai.storyengine.core.application.usecase.world;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.port.inbound.world.DeleteWorldLorebookEntry;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@CommandHandler
public class DeleteWorldLorebookEntryHandler extends AbstractCommandHandler<DeleteWorldLorebookEntry, Void> {

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

        if (command.entryId() == null) {
            throw new IllegalArgumentException(ENTRY_ID_CANNOT_BE_NULL_OR_EMPTY);
        }

        if (command.worldId() == null) {
            throw new IllegalArgumentException(WORLD_ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public Void execute(DeleteWorldLorebookEntry command) {

        World world = repository.findByPublicId(command.worldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_TO_BE_UPDATED_WAS_NOT_FOUND));

        // TODO externalize to authorizer
        if (!world.canUserWrite(command.requesterId())) {
            throw new AssetAccessDeniedException(USER_DOES_NOT_HAVE_PERMISSION_TO_MODIFY_THIS_WORLD);
        }

        world.removeLorebookEntry(command.entryId());
        repository.save(world);

        return null;
    }
}
