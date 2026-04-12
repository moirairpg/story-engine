package me.moirai.storyengine.core.application.query.adventure;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureSummary;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventures;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureSearchReader;

@QueryHandler
public class SearchAdventuresHandler extends AbstractQueryHandler<SearchAdventures, PaginatedResult<AdventureSummary>> {

    private final AdventureSearchReader reader;

    public SearchAdventuresHandler(AdventureSearchReader reader) {
        this.reader = reader;
    }

    @Override
    public PaginatedResult<AdventureSummary> execute(SearchAdventures query) {

        var rows = reader.search(query);

        var summaries = rows.data().stream()
                .map(row -> {
                    var canWrite = PermissionLevel.WRITE.name().equals(row.userPermission())
                            || PermissionLevel.OWNER.name().equals(row.userPermission());

                    return new AdventureSummary(
                            row.id(),
                            row.name(),
                            row.description(),
                            row.worldName(),
                            row.narratorName(),
                            row.visibility(),
                            row.creationDate(),
                            canWrite);
                })
                .toList();

        return new PaginatedResult<>(summaries, rows.items(), rows.totalItems(), rows.page(), rows.totalPages());
    }
}
