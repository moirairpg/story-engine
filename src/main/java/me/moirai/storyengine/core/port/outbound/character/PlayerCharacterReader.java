package me.moirai.storyengine.core.port.outbound.character;

import java.util.Optional;
import java.util.UUID;

public interface PlayerCharacterReader {

    Optional<PlayerCharacterDetailsRow> getById(UUID characterId);

    Optional<String> getOwnerUsername(UUID characterId);
}