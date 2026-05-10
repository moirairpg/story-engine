package me.moirai.storyengine.core.port.inbound.character;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.query.Query;

public record GetPlayerCharacterById(UUID characterId)
        implements Query<PlayerCharacterDetails> {
}