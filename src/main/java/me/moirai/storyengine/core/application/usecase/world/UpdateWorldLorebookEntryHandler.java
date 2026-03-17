package me.moirai.storyengine.core.application.usecase.world;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@CommandHandler
public class UpdateWorldLorebookEntryHandler
        extends AbstractCommandHandler<UpdateWorldLorebookEntry, WorldLorebookEntryDetails> {

    private static final String WORLD_TO_BE_UPDATED_WAS_NOT_FOUND = "World to be updated was not found";
    private static final String USER_DOES_NOT_HAVE_PERMISSION_TO_MODIFY_THIS_WORLD = "User does not have permission to modify this world";

    private final WorldRepository repository;

    public UpdateWorldLorebookEntryHandler(WorldRepository repository) {

        this.repository = repository;
    }

    @Override
    public WorldLorebookEntryDetails execute(UpdateWorldLorebookEntry command) {

        World world = repository.findByPublicId(command.worldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_TO_BE_UPDATED_WAS_NOT_FOUND));

        // TODO externalize to authorizer
        if (!world.canUserWrite(command.requesterId())) {
            throw new AssetAccessDeniedException(USER_DOES_NOT_HAVE_PERMISSION_TO_MODIFY_THIS_WORLD);
        }

        WorldLorebookEntry lorebookEntry = world.updateLorebookEntry(
                command.entryId(),
                command.name(),
                command.regex(),
                command.requesterId());

        repository.save(world);
        return mapResult(world, lorebookEntry);
    }

    private WorldLorebookEntryDetails mapResult(World world, WorldLorebookEntry savedEntry) {

        return new WorldLorebookEntryDetails(
                savedEntry.getPublicId(),
                world.getPublicId(),
                savedEntry.getName(),
                savedEntry.getRegex(),
                savedEntry.getDescription(),
                savedEntry.getCreationDate(),
                savedEntry.getLastUpdateDate());
    }
}
