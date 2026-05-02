package me.moirai.storyengine.core.application.query.world;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.world.GetWorldById;
import me.moirai.storyengine.core.port.inbound.world.WorldDetails;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import me.moirai.storyengine.core.port.outbound.world.WorldReader;

@QueryHandler
public class GetWorldByIdHandler extends AbstractQueryHandler<GetWorldById, WorldDetails> {

    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "World ID cannot be null or empty";
    private static final String WORLD_NOT_FOUND = "World to be deleted was not found";

    private final WorldReader reader;
    private final StoragePort storagePort;

    public GetWorldByIdHandler(
            WorldReader reader,
            StoragePort storagePort) {

        this.reader = reader;
        this.storagePort = storagePort;
    }

    @Override
    public void validate(GetWorldById request) {

        if (request.worldId() == null) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public WorldDetails execute(GetWorldById query) {

        var world = reader.getWorldById(query.worldId())
                .orElseThrow(() -> new NotFoundException(WORLD_NOT_FOUND));

        return new WorldDetails(
                world.id(),
                world.name(),
                world.description(),
                world.adventureStart(),
                world.narratorName(),
                world.narratorPersonality(),
                world.visibility(),
                storagePort.resolveUrl(world.imageKey()),
                world.permissions(),
                world.lorebook(),
                world.creationDate(),
                world.lastUpdateDate(),
                world.uiImagePositionX(),
                world.uiImagePositionY());
    }
}
