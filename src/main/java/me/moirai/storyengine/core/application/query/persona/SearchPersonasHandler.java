package me.moirai.storyengine.core.application.query.persona;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.core.port.inbound.persona.PersonaSummary;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonas;
import me.moirai.storyengine.core.port.outbound.persona.PersonaSearchReader;

@QueryHandler
public class SearchPersonasHandler extends AbstractQueryHandler<SearchPersonas, PaginatedResult<PersonaSummary>> {

    private final PersonaSearchReader reader;

    public SearchPersonasHandler(PersonaSearchReader reader) {
        this.reader = reader;
    }

    @Override
    public PaginatedResult<PersonaSummary> execute(SearchPersonas query) {

        var rows = reader.search(query);

        var summaries = rows.data().stream()
                .map(row -> {
                    var canWrite = PermissionLevel.WRITE.name().equals(row.userPermission())
                            || PermissionLevel.OWNER.name().equals(row.userPermission());

                    return new PersonaSummary(
                            row.id(),
                            row.name(),
                            row.personality(),
                            row.visibility(),
                            row.creationDate(),
                            canWrite);
                })
                .toList();

        return new PaginatedResult<>(summaries, rows.items(), rows.totalItems(), rows.page(), rows.totalPages());
    }
}
