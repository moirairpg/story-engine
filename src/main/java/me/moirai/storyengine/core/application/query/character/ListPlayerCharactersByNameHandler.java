package me.moirai.storyengine.core.application.query.character;

import java.util.List;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.core.port.inbound.character.ListPlayerCharactersByName;
import me.moirai.storyengine.core.port.inbound.character.PlayerCharacterSummary;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterSearchReader;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;

@QueryHandler
public class ListPlayerCharactersByNameHandler
        extends AbstractQueryHandler<ListPlayerCharactersByName, List<PlayerCharacterSummary>> {

    private final PlayerCharacterSearchReader reader;
    private final StoragePort storagePort;

    public ListPlayerCharactersByNameHandler(PlayerCharacterSearchReader reader, StoragePort storagePort) {

        this.reader = reader;
        this.storagePort = storagePort;
    }

    @Override
    public List<PlayerCharacterSummary> execute(ListPlayerCharactersByName query) {

        return reader.listByName(query.name(), query.requesterId()).stream()
                .map(row -> new PlayerCharacterSummary(
                        row.id(),
                        row.ownerUsername(),
                        row.ownerDisplayName(),
                        row.name(),
                        row.characterClass(),
                        row.background(),
                        storagePort.resolveUrl(row.imageKey()),
                        row.uiImagePositionX(),
                        row.uiImagePositionY()))
                .toList();
    }
}
