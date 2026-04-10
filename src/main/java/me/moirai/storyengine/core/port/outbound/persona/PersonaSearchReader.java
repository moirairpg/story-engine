package me.moirai.storyengine.core.port.outbound.persona;

import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonas;

public interface PersonaSearchReader {

    PaginatedResult<PersonaSearchRow> search(SearchPersonas query);
}
