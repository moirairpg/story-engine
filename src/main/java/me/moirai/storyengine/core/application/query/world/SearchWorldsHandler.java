package me.moirai.storyengine.core.application.query.world;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.core.port.inbound.world.SearchWorlds;
import me.moirai.storyengine.core.port.inbound.world.WorldSummary;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import me.moirai.storyengine.core.port.outbound.world.WorldSearchReader;

@QueryHandler
public class SearchWorldsHandler extends AbstractQueryHandler<SearchWorlds, PaginatedResult<WorldSummary>> {

    private final WorldSearchReader reader;
    private final StoragePort storagePort;

    public SearchWorldsHandler(
            WorldSearchReader reader,
            StoragePort storagePort) {

        this.reader = reader;
        this.storagePort = storagePort;
    }

    @Override
    public PaginatedResult<WorldSummary> execute(SearchWorlds query) {

        var rows = reader.search(query);

        var summaries = rows.data().stream()
                .map(row -> {
                    var canWrite = PermissionLevel.WRITE.name().equals(row.userPermission())
                            || PermissionLevel.OWNER.name().equals(row.userPermission());

                    return new WorldSummary(
                            row.id(),
                            row.name(),
                            row.description(),
                            row.visibility(),
                            row.creationDate(),
                            storagePort.resolveUrl(row.imageKey()),
                            canWrite);
                })
                .toList();

        return new PaginatedResult<>(summaries, rows.items(), rows.totalItems(), rows.page(), rows.totalPages());
    }
}
