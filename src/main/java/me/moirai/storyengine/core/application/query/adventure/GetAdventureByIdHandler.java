package me.moirai.storyengine.core.application.query.adventure;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureById;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureReader;

@QueryHandler
public class GetAdventureByIdHandler extends AbstractQueryHandler<GetAdventureById, AdventureDetails> {

    private static final String ADVENTURE_NOT_FOUND = "Adventure to be viewed was not found";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "Adventure ID cannot be null or empty";

    private final AdventureReader reader;

    public GetAdventureByIdHandler(AdventureReader reader) {
        this.reader = reader;
    }

    @Override
    public void validate(GetAdventureById command) {

        if (command.adventureId() == null) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public AdventureDetails execute(GetAdventureById query) {

        return reader.getAdventureById(query.adventureId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_NOT_FOUND));
    }
}
