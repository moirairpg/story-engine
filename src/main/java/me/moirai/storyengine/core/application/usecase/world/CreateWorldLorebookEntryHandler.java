package me.moirai.storyengine.core.application.usecase.world;

import io.micrometer.common.util.StringUtils;
import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.CreateWorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@UseCaseHandler
public class CreateWorldLorebookEntryHandler
        extends AbstractUseCaseHandler<CreateWorldLorebookEntry, WorldLorebookEntryDetails> {

    private static final String WORLD_TO_BE_UPDATED_WAS_NOT_FOUND = "World to be updated was not found";
    private static final String USER_DOES_NOT_HAVE_PERMISSION_TO_MODIFY_THIS_WORLD = "User does not have permission to modify this world";

    private final WorldRepository repository;

    public CreateWorldLorebookEntryHandler(WorldRepository repository) {

        this.repository = repository;
    }

    @Override
    public void validate(CreateWorldLorebookEntry command) {

        if (StringUtils.isBlank(command.getWorldId())) {
            throw new IllegalArgumentException("World ID cannot be null");
        }

        if (StringUtils.isBlank(command.getName())) {
            throw new IllegalArgumentException("Name cannot be null");
        }

        if (StringUtils.isBlank(command.getDescription())) {
            throw new IllegalArgumentException("Description cannot be null");
        }
    }

    @Override
    public WorldLorebookEntryDetails execute(CreateWorldLorebookEntry command) {

        World world = repository.findByPublicId(command.getWorldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_TO_BE_UPDATED_WAS_NOT_FOUND));

        if (!world.canUserWrite(command.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_DOES_NOT_HAVE_PERMISSION_TO_MODIFY_THIS_WORLD);
        }

        WorldLorebookEntry entry = world.addLorebookEntry(
                command.getName(),
                command.getRegex(),
                command.getDescription());

        repository.save(world);
        return mapResult(entry);
    }

    private WorldLorebookEntryDetails mapResult(WorldLorebookEntry entry) {

        return WorldLorebookEntryDetails.builder()
                .id(entry.getPublicId())
                .name(entry.getName())
                .regex(entry.getRegex())
                .description(entry.getDescription())
                .creationDate(entry.getCreationDate())
                .lastUpdateDate(entry.getLastUpdateDate())
                .build();
    }
}
