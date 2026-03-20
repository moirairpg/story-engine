package me.moirai.storyengine.core.application.query.world;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.port.inbound.world.GetWorldById;
import me.moirai.storyengine.core.port.inbound.world.WorldDetails;
import me.moirai.storyengine.core.port.outbound.world.WorldReader;

@QueryHandler
public class GetWorldByIdHandler extends AbstractQueryHandler<GetWorldById, WorldDetails> {

    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "World ID cannot be null or empty";
    private static final String WORLD_NOT_FOUND = "World to be deleted was not found";

    private final WorldReader reader;

    public GetWorldByIdHandler(WorldReader reader) {
        this.reader = reader;
    }

    @Override
    public void validate(GetWorldById request) {

        if (request.worldId() == null) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public WorldDetails execute(GetWorldById query) {

        return reader.getWorldById(query.worldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_NOT_FOUND));
    }
}
