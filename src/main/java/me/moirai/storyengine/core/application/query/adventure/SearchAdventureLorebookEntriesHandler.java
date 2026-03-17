package me.moirai.storyengine.core.application.query.adventure;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntries;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntriesResult;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@QueryHandler
public class SearchAdventureLorebookEntriesHandler
        extends AbstractQueryHandler<SearchAdventureLorebookEntries, SearchAdventureLorebookEntriesResult> {

    private static final String USER_DOES_NO_PERMISSION = "User does not have permission to view this adventure";
    private static final String ADVENTURE_NOT_FOUND = "The adventure where the entries are being search doesn't exist";

    private final AdventureRepository adventureRepository;

    public SearchAdventureLorebookEntriesHandler(AdventureRepository adventureRepository) {

        this.adventureRepository = adventureRepository;
    }

    @Override
    public SearchAdventureLorebookEntriesResult execute(SearchAdventureLorebookEntries query) {

        var adventure = adventureRepository.findByPublicId(query.adventureId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_NOT_FOUND));

        // TODO externalize to authorizer
        if (adventure.canUserRead(query.requesterId())) {
            return adventureRepository.searchLorebookEntries(query);
        }

        throw new AssetAccessDeniedException(USER_DOES_NO_PERMISSION);
    }
}
