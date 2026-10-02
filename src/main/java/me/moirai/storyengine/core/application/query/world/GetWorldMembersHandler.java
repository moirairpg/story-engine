package me.moirai.storyengine.core.application.query.world;

import java.util.List;

import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.port.inbound.AssetMember;
import me.moirai.storyengine.core.port.inbound.world.GetWorldMembers;
import me.moirai.storyengine.core.port.outbound.world.WorldPermissionReader;

@QueryHandler
@Authorize(operation = AuthorizationOperation.MANAGE_WORLD_PERMISSIONS, fields = "#request.worldId")
public class GetWorldMembersHandler extends AbstractQueryHandler<GetWorldMembers, List<AssetMember>> {

    private final WorldPermissionReader reader;

    public GetWorldMembersHandler(WorldPermissionReader reader) {
        this.reader = reader;
    }

    @Override
    public List<AssetMember> execute(GetWorldMembers query) {

        return reader.getAllByWorldPublicId(query.worldId());
    }
}
