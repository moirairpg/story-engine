package me.moirai.storyengine.core.application.query.persona;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.dto.PaginatedResult;
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

        return reader.search(query);
    }
}
