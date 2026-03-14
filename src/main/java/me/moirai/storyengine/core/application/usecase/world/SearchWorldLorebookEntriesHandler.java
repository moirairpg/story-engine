package me.moirai.storyengine.core.application.usecase.world;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntries;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntriesResult;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntryRepository;
import me.moirai.storyengine.core.domain.world.WorldRepository;

@UseCaseHandler
public class SearchWorldLorebookEntriesHandler
        extends AbstractUseCaseHandler<SearchWorldLorebookEntries, SearchWorldLorebookEntriesResult> {

    private static final String USER_DOES_NO_PERMISSION = "User does not have permission to view this world";
    private static final String WORLD_NOT_FOUND = "The world where the entries are being search doesn't exist";

    private final WorldRepository worldRepository;
    private final WorldLorebookEntryRepository repository;

    public SearchWorldLorebookEntriesHandler(
            WorldRepository worldRepository,
            WorldLorebookEntryRepository repository) {

        this.worldRepository = worldRepository;
        this.repository = repository;
    }

    @Override
    public SearchWorldLorebookEntriesResult execute(SearchWorldLorebookEntries query) {

        World world = worldRepository.findById(query.getWorldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_NOT_FOUND));

        if (world.canUserRead(query.getRequesterDiscordId())) {
            return repository.search(query);
        }

        throw new AssetAccessDeniedException(USER_DOES_NO_PERMISSION);
    }
}