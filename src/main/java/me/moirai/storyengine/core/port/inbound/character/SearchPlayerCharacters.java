package me.moirai.storyengine.core.port.inbound.character;

import me.moirai.storyengine.common.cqs.query.Query;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.enums.CharacterClass;
import me.moirai.storyengine.common.enums.PlayerCharacterSortField;
import me.moirai.storyengine.common.enums.SortDirection;

public record SearchPlayerCharacters(
        String name,
        CharacterClass characterClass,
        PlayerCharacterSortField sortingField,
        SortDirection direction,
        Integer page,
        Integer size,
        Long requesterId)
        implements Query<PaginatedResult<PlayerCharacterSummary>> {
}