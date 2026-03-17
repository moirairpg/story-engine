package me.moirai.storyengine.core.application.query.world;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntries;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntriesResult;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@QueryHandler
public class SearchWorldLorebookEntriesHandler
        extends AbstractQueryHandler<SearchWorldLorebookEntries, SearchWorldLorebookEntriesResult> {

    private static final String USER_DOES_NO_PERMISSION = "User does not have permission to view this world";
    private static final String WORLD_NOT_FOUND = "The world where the entries are being search doesn't exist";

    private final WorldRepository worldRepository;

    public SearchWorldLorebookEntriesHandler(WorldRepository worldRepository) {

        this.worldRepository = worldRepository;
    }

    @Override
    public SearchWorldLorebookEntriesResult execute(SearchWorldLorebookEntries query) {

        var world = worldRepository.findByPublicId(query.worldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_NOT_FOUND));

        if (world.canUserRead(query.requesterId())) {
            return worldRepository.searchLorebookEntries(query);
        }

        throw new AssetAccessDeniedException(USER_DOES_NO_PERMISSION);
    }
}
