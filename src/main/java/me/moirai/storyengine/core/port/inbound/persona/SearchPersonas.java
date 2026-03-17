package me.moirai.storyengine.core.port.inbound.persona;

import me.moirai.storyengine.common.cqs.query.Query;
import me.moirai.storyengine.common.enums.Visibility;

public record SearchPersonas(
        String name,
        String ownerId,
        Integer page,
        Integer size,
        String sortingField, // TODO create enum
        String direction, // TODO create enum
        Visibility visibility,
        String operation,
        String requesterId) implements Query<SearchPersonasResult> {
}