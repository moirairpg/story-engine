package me.moirai.storyengine.core.application.usecase.adventure;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntries;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntriesResult;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntryRepository;
import me.moirai.storyengine.core.domain.adventure.AdventureRepository;

@UseCaseHandler
public class SearchAdventureLorebookEntriesHandler
        extends AbstractUseCaseHandler<SearchAdventureLorebookEntries, SearchAdventureLorebookEntriesResult> {

    private static final String USER_DOES_NO_PERMISSION = "User does not have permission to view this adventure";
    private static final String ADVENTURE_NOT_FOUND = "The adventure where the entries are being search doesn't exist";

    private final AdventureRepository adventureRepository;
    private final AdventureLorebookEntryRepository repository;

    public SearchAdventureLorebookEntriesHandler(AdventureRepository adventureRepository,
            AdventureLorebookEntryRepository repository) {

        this.adventureRepository = adventureRepository;
        this.repository = repository;
    }

    @Override
    public SearchAdventureLorebookEntriesResult execute(SearchAdventureLorebookEntries query) {

        Adventure adventure = adventureRepository.findById(query.getAdventureId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_NOT_FOUND));

        if (adventure.canUserRead(query.getRequesterDiscordId())) {
            return repository.search(query);
        }

        throw new AssetAccessDeniedException(USER_DOES_NO_PERMISSION);
    }
}
