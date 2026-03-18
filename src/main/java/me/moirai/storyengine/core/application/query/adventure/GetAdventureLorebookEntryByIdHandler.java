package me.moirai.storyengine.core.application.query.adventure;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntry;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureLorebookEntryById;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@QueryHandler
public class GetAdventureLorebookEntryByIdHandler
        extends AbstractQueryHandler<GetAdventureLorebookEntryById, AdventureLorebookEntryDetails> {

    private static final String ADVENTURE_TO_BE_VIEWED_WAS_NOT_FOUND = "Adventure to be viewed was not found";

    private final AdventureRepository repository;

    public GetAdventureLorebookEntryByIdHandler(AdventureRepository repository) {

        this.repository = repository;
    }

    @Override
    public void validate(GetAdventureLorebookEntryById query) {

        if (query.entryId() == null) {
            throw new IllegalArgumentException("Lorebook entry ID cannot be null");
        }

        if (query.adventureId() == null) {
            throw new IllegalArgumentException("Adventure ID cannot be null");
        }
    }

    @Override
    public AdventureLorebookEntryDetails execute(GetAdventureLorebookEntryById query) {

        var adventure = repository.findByPublicId(query.adventureId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_TO_BE_VIEWED_WAS_NOT_FOUND));

        var entry = adventure.getLorebookEntryById(query.entryId());

        return mapResult(entry);
    }

    private AdventureLorebookEntryDetails mapResult(AdventureLorebookEntry entry) {

        return new AdventureLorebookEntryDetails(
                entry.getPublicId(),
                null,
                entry.getName(),
                entry.getRegex(),
                entry.getDescription(),
                entry.getPlayerId(),
                entry.isPlayerCharacter(),
                entry.getCreationDate(),
                entry.getLastUpdateDate());
    }
}
