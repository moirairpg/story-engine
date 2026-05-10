package me.moirai.storyengine.core.application.query.character;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.core.port.inbound.character.PlayerCharacterSummary;
import me.moirai.storyengine.core.port.inbound.character.SearchPlayerCharacters;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterSearchReader;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;

@QueryHandler
public class SearchPlayerCharactersHandler
        extends AbstractQueryHandler<SearchPlayerCharacters, PaginatedResult<PlayerCharacterSummary>> {

    private final PlayerCharacterSearchReader reader;
    private final StoragePort storagePort;

    public SearchPlayerCharactersHandler(PlayerCharacterSearchReader reader, StoragePort storagePort) {

        this.reader = reader;
        this.storagePort = storagePort;
    }

    @Override
    public PaginatedResult<PlayerCharacterSummary> execute(SearchPlayerCharacters query) {

        var page = reader.search(query);
        var characters = page.data().stream()
                .map(row -> new PlayerCharacterSummary(
                        row.id(),
                        row.ownerUsername(),
                        row.name(),
                        row.characterClass(),
                        storagePort.resolveUrl(row.imageKey())))
                .toList();

        return new PaginatedResult<>(characters, page.items(), page.totalItems(), page.page(), page.totalPages());
    }
}