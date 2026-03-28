package me.moirai.storyengine.core.application.query.adventure;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureLorebookEntryById;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureLorebookReader;

@QueryHandler
public class GetAdventureLorebookEntryByIdHandler
        extends AbstractQueryHandler<GetAdventureLorebookEntryById, AdventureLorebookEntryDetails> {

    private static final String ADVENTURE_LOREBOOK_ENTRY_NOT_FOUND = "Adventure lorebook entry was not found";

    private final AdventureLorebookReader reader;

    public GetAdventureLorebookEntryByIdHandler(AdventureLorebookReader reader) {

        this.reader = reader;
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

        return reader.getAdventureLorebookEntryById(query.entryId(), query.adventureId())
                .orElseThrow(() -> new NotFoundException(ADVENTURE_LOREBOOK_ENTRY_NOT_FOUND));
    }
}
