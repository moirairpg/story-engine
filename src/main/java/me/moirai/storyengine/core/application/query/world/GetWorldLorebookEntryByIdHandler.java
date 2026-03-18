package me.moirai.storyengine.core.application.query.world;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.GetWorldLorebookEntryById;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@QueryHandler
public class GetWorldLorebookEntryByIdHandler
        extends AbstractQueryHandler<GetWorldLorebookEntryById, WorldLorebookEntryDetails> {

    private static final String WORLD_TO_BE_VIEWED_WAS_NOT_FOUND = "World to be viewed was not found";

    private final WorldRepository repository;

    public GetWorldLorebookEntryByIdHandler(WorldRepository repository) {

        this.repository = repository;
    }

    @Override
    public void validate(GetWorldLorebookEntryById query) {

        if (query.entryId() == null) {
            throw new IllegalArgumentException("Lorebook entry ID cannot be null");
        }

        if (query.worldId() == null) {
            throw new IllegalArgumentException("World ID cannot be null");
        }
    }

    @Override
    public WorldLorebookEntryDetails execute(GetWorldLorebookEntryById query) {

        var world = repository.findByPublicId(query.worldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_TO_BE_VIEWED_WAS_NOT_FOUND));

        var entry = world.getLorebookEntryById(query.entryId());

        return mapResult(world, entry);
    }

    private WorldLorebookEntryDetails mapResult(World world, WorldLorebookEntry entry) {

        return new WorldLorebookEntryDetails(
                entry.getPublicId(),
                world.getPublicId(),
                entry.getName(),
                entry.getRegex(),
                entry.getDescription(),
                entry.getCreationDate(),
                entry.getLastUpdateDate());
    }
}
