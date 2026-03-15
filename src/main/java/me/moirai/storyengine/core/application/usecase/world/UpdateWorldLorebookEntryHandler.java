package me.moirai.storyengine.core.application.usecase.world;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@UseCaseHandler
public class UpdateWorldLorebookEntryHandler
        extends AbstractUseCaseHandler<UpdateWorldLorebookEntry, WorldLorebookEntryDetails> {

    private static final String WORLD_TO_BE_UPDATED_WAS_NOT_FOUND = "World to be updated was not found";
    private static final String USER_DOES_NOT_HAVE_PERMISSION_TO_MODIFY_THIS_WORLD = "User does not have permission to modify this world";

    private final WorldRepository repository;

    public UpdateWorldLorebookEntryHandler(WorldRepository repository) {

        this.repository = repository;
    }

    @Override
    public WorldLorebookEntryDetails execute(UpdateWorldLorebookEntry command) {

        World world = repository.findByPublicId(command.getWorldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_TO_BE_UPDATED_WAS_NOT_FOUND));

        if (!world.canUserWrite(command.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_DOES_NOT_HAVE_PERMISSION_TO_MODIFY_THIS_WORLD);
        }

        WorldLorebookEntry lorebookEntry = world.updateLorebookEntry(
                command.getId(),
                command.getName(),
                command.getRegex(),
                command.getDescription());

        repository.save(world);
        return mapResult(lorebookEntry);
    }

    private WorldLorebookEntryDetails mapResult(WorldLorebookEntry savedEntry) {

        return WorldLorebookEntryDetails.builder()
                .id(savedEntry.getPublicId())
                .name(savedEntry.getName())
                .regex(savedEntry.getRegex())
                .description(savedEntry.getDescription())
                .creationDate(savedEntry.getCreationDate())
                .lastUpdateDate(savedEntry.getLastUpdateDate())
                .build();
    }
}
