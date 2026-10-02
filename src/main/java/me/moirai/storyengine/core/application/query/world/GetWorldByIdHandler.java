package me.moirai.storyengine.core.application.query.world;

import static me.moirai.storyengine.common.enums.PermissionLevel.OWNER;
import static me.moirai.storyengine.common.enums.PermissionLevel.WRITE;

import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.port.inbound.world.GetWorldById;
import me.moirai.storyengine.core.port.inbound.world.WorldDetails;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import me.moirai.storyengine.core.port.outbound.world.WorldReader;

@QueryHandler
@Authorize(operation = AuthorizationOperation.VIEW_WORLD, fields = "#request.worldId")
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

        var permissions = world.permissions();

        var isOwner = permissions.stream()
                .anyMatch(permission -> permission.level() == OWNER
                        && permission.userId().equals(query.requesterId()));

        var canManage = isOwner || permissions.stream()
                .anyMatch(permission -> permission.level() == WRITE
                        && permission.userId().equals(query.requesterId()));

        return new WorldDetails(
                world.id(),
                world.name(),
                world.description(),
                world.adventureStart(),
                world.narratorName(),
                world.narratorPersonality(),
                world.visibility(),
                storagePort.resolveUrl(world.imageKey()),
                canManage,
                isOwner,
                world.lorebook(),
                world.creationDate(),
                world.lastUpdateDate(),
                world.uiImagePositionX(),
                world.uiImagePositionY());
    }
}
