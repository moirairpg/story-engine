package me.moirai.storyengine.core.application.usecase.adventure;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntries;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntriesResult;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.domain.adventure.Adventure;

@UseCaseHandler
public class SearchAdventureLorebookEntriesHandler
        extends AbstractUseCaseHandler<SearchAdventureLorebookEntries, SearchAdventureLorebookEntriesResult> {

    private static final String USER_DOES_NO_PERMISSION = "User does not have permission to view this adventure";
    private static final String ADVENTURE_NOT_FOUND = "The adventure where the entries are being search doesn't exist";

    private final AdventureRepository adventureRepository;

    public SearchAdventureLorebookEntriesHandler(AdventureRepository adventureRepository) {

        this.adventureRepository = adventureRepository;
    }

    @Override
    public SearchAdventureLorebookEntriesResult execute(SearchAdventureLorebookEntries query) {

        Adventure adventure = adventureRepository.findById(query.getAdventureId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_NOT_FOUND));

        if (adventure.canUserRead(query.getRequesterDiscordId())) {
            return adventureRepository.searchLorebookEntries(query);
        }

        throw new AssetAccessDeniedException(USER_DOES_NO_PERMISSION);
    }
}
