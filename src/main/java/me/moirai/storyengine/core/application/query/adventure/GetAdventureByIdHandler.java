package me.moirai.storyengine.core.application.query.adventure;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureById;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureReader;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;

@QueryHandler
public class GetAdventureByIdHandler extends AbstractQueryHandler<GetAdventureById, AdventureDetails> {

    private static final String ADVENTURE_NOT_FOUND = "Adventure to be viewed was not found";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "Adventure ID cannot be null or empty";

    private final AdventureReader reader;
    private final StoragePort storagePort;

    public GetAdventureByIdHandler(
            AdventureReader reader,
            StoragePort storagePort) {

        this.reader = reader;
        this.storagePort = storagePort;
    }

    @Override
    public void validate(GetAdventureById command) {

        if (command.adventureId() == null) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public AdventureDetails execute(GetAdventureById query) {

        var adventure = reader.getAdventureById(query.adventureId())
                .orElseThrow(() -> new NotFoundException(ADVENTURE_NOT_FOUND));

        return new AdventureDetails(
                adventure.id(),
                adventure.name(),
                adventure.description(),
                adventure.adventureStart(),
                adventure.worldId(),
                adventure.narratorName(),
                adventure.narratorPersonality(),
                adventure.visibility(),
                adventure.moderation(),
                adventure.isMultiplayer(),
                storagePort.resolveUrl(adventure.imageKey()),
                adventure.creationDate(),
                adventure.lastUpdateDate(),
                adventure.modelConfiguration(),
                adventure.contextAttributes(),
                adventure.permissions(),
                adventure.lorebook(),
                adventure.uiImagePositionX(),
                adventure.uiImagePositionY());
    }
}
