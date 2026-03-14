package me.moirai.storyengine.core.application.usecase.world;

import io.micrometer.common.util.StringUtils;
import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;
import me.moirai.storyengine.core.port.outbound.world.WorldLorebookEntryRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@UseCaseHandler
public class UpdateWorldLorebookEntryHandler
        extends AbstractUseCaseHandler<UpdateWorldLorebookEntry, WorldLorebookEntryDetails> {

    private static final String WORLD_TO_BE_UPDATED_WAS_NOT_FOUND = "World to be updated was not found";
    private static final String USER_DOES_NOT_HAVE_PERMISSION_TO_MODIFY_THIS_WORLD = "User does not have permission to modify this world";
    private static final String LOREBOOK_ENTRY_TO_BE_UPDATED_WAS_NOT_FOUND = "Lorebook entry to be updated was not found";

    private final WorldLorebookEntryRepository lorebookEntryRepository;
    private final WorldRepository repository;

    public UpdateWorldLorebookEntryHandler(
            WorldLorebookEntryRepository lorebookEntryRepository,
            WorldRepository repository) {

        this.lorebookEntryRepository = lorebookEntryRepository;
        this.repository = repository;
    }

    @Override
    public WorldLorebookEntryDetails execute(UpdateWorldLorebookEntry command) {

        World world = repository.findById(command.getWorldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_TO_BE_UPDATED_WAS_NOT_FOUND));

        if (!world.canUserWrite(command.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_DOES_NOT_HAVE_PERMISSION_TO_MODIFY_THIS_WORLD);
        }

        WorldLorebookEntry lorebookEntry = lorebookEntryRepository.findById(command.getId())
                .orElseThrow(() -> new AssetNotFoundException(LOREBOOK_ENTRY_TO_BE_UPDATED_WAS_NOT_FOUND));

        if (StringUtils.isNotBlank(command.getName())) {
            lorebookEntry.updateName(command.getName());
        }

        if (StringUtils.isNotBlank(command.getRegex())) {
            lorebookEntry.updateRegex(command.getRegex());
        }

        if (StringUtils.isNotBlank(command.getDescription())) {
            lorebookEntry.updateDescription(command.getDescription());
        }

        return mapResult(lorebookEntryRepository.save(lorebookEntry));
    }

    private WorldLorebookEntryDetails mapResult(WorldLorebookEntry savedEntry) {

        return WorldLorebookEntryDetails.builder()
                .id(savedEntry.getId())
                .name(savedEntry.getName())
                .regex(savedEntry.getRegex())
                .description(savedEntry.getDescription())
                .creationDate(savedEntry.getCreationDate())
                .lastUpdateDate(savedEntry.getLastUpdateDate())
                .build();
    }
}
