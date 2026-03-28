package me.moirai.storyengine.core.application.query.world;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.world.GetWorldLorebookEntryById;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;
import me.moirai.storyengine.core.port.outbound.world.WorldLorebookReader;

@QueryHandler
public class GetWorldLorebookEntryByIdHandler
        extends AbstractQueryHandler<GetWorldLorebookEntryById, WorldLorebookEntryDetails> {

    private static final String WORLD_LOREBOOK_ENTRY_NOT_FOUND = "World lorebook entry was not found";

    private final WorldLorebookReader reader;

    public GetWorldLorebookEntryByIdHandler(WorldLorebookReader reader) {

        this.reader = reader;
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

        return reader.getWorldLorebookEntryById(query.entryId(), query.worldId())
                .orElseThrow(() -> new NotFoundException(WORLD_LOREBOOK_ENTRY_NOT_FOUND));
    }
}
