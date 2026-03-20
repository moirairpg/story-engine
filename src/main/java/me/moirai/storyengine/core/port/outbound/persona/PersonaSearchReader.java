package me.moirai.storyengine.core.port.outbound.persona;

import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.core.port.inbound.persona.PersonaSummary;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonas;

public interface PersonaSearchReader {

    PaginatedResult<PersonaSummary> search(SearchPersonas query);
}
