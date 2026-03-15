package me.moirai.storyengine.core.application.usecase.world;

import org.apache.commons.lang3.StringUtils;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.GetWorldLorebookEntryById;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@UseCaseHandler
public class GetWorldLorebookEntryByIdHandler extends AbstractUseCaseHandler<GetWorldLorebookEntryById, WorldLorebookEntryDetails> {

    private static final String WORLD_TO_BE_VIEWED_WAS_NOT_FOUND = "World to be viewed was not found";
    private static final String USER_DOES_NOT_HAVE_PERMISSION_TO_VIEW_THIS_WORLD = "User does not have permission to view this world";

    private final WorldRepository repository;

    public GetWorldLorebookEntryByIdHandler(WorldRepository repository) {

        this.repository = repository;
    }

    @Override
    public void validate(GetWorldLorebookEntryById query) {

        if (StringUtils.isBlank(query.getEntryId())) {
            throw new IllegalArgumentException("Lorebook entry ID cannot be null");
        }

        if (StringUtils.isBlank(query.getWorldId())) {
            throw new IllegalArgumentException("World ID cannot be null");
        }
    }

    @Override
    public WorldLorebookEntryDetails execute(GetWorldLorebookEntryById query) {

        World world = repository.findById(query.getWorldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_TO_BE_VIEWED_WAS_NOT_FOUND));

        if (!world.canUserRead(query.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_DOES_NOT_HAVE_PERMISSION_TO_VIEW_THIS_WORLD);
        }

        WorldLorebookEntry entry = world.getLorebookEntryById(query.getEntryId());

        return mapResult(entry);
    }

    private WorldLorebookEntryDetails mapResult(WorldLorebookEntry entry) {

        return WorldLorebookEntryDetails.builder()
                .id(entry.getId())
                .name(entry.getName())
                .regex(entry.getRegex())
                .description(entry.getDescription())
                .creationDate(entry.getCreationDate())
                .lastUpdateDate(entry.getLastUpdateDate())
                .build();
    }
}
