package me.moirai.storyengine.core.application.query.character;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.character.GetPlayerCharacterById;
import me.moirai.storyengine.core.port.inbound.character.PlayerCharacterDetails;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterReader;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;

@QueryHandler
public class GetPlayerCharacterByIdHandler
        extends AbstractQueryHandler<GetPlayerCharacterById, PlayerCharacterDetails> {

    private final PlayerCharacterReader reader;
    private final StoragePort storagePort;

    public GetPlayerCharacterByIdHandler(PlayerCharacterReader reader, StoragePort storagePort) {

        this.reader = reader;
        this.storagePort = storagePort;
    }

    @Override
    public PlayerCharacterDetails execute(GetPlayerCharacterById query) {

        var row = reader.getById(query.characterId())
                .orElseThrow(() -> new NotFoundException("Player character not found"));

        return new PlayerCharacterDetails(
                row.id(),
                row.ownerUsername(),
                row.name(),
                row.characterClass(),
                row.personality(),
                row.physicalDescription(),
                storagePort.resolveUrl(row.imageKey()),
                row.creationDate(),
                row.lastUpdateDate());
    }
}