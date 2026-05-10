package me.moirai.storyengine.core.port.outbound.character;

import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.core.port.inbound.character.SearchPlayerCharacters;

public interface PlayerCharacterSearchReader {

    PaginatedResult<PlayerCharacterSummaryRow> search(SearchPlayerCharacters query);
}