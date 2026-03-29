package me.moirai.storyengine.core.application.command.adventure;

import static io.micrometer.common.util.StringUtils.isBlank;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntry;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventureLorebookEntry;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.generation.EmbeddingPort;
import me.moirai.storyengine.core.port.outbound.vectorsearch.VectorSearchPort;

@CommandHandler
public class CreateAdventureLorebookEntryHandler
        extends AbstractCommandHandler<CreateAdventureLorebookEntry, AdventureLorebookEntryDetails> {

    private static final String ADVENTURE_NOT_FOUND = "Adventure to be updated was not found";

    private final AdventureRepository repository;
    private final EmbeddingPort embeddingPort;
    private final VectorSearchPort vectorSearchPort;

    public CreateAdventureLorebookEntryHandler(
            AdventureRepository repository,
            EmbeddingPort embeddingPort,
            VectorSearchPort vectorSearchPort) {

        this.repository = repository;
        this.embeddingPort = embeddingPort;
        this.vectorSearchPort = vectorSearchPort;
    }

    @Override
    public void validate(CreateAdventureLorebookEntry command) {

        if (command.adventureId() == null) {
            throw new IllegalArgumentException("Adventure ID cannot be null");
        }

        if (isBlank(command.name())) {
            throw new IllegalArgumentException("Adventure name cannot be null");
        }

        if (isBlank(command.description())) {
            throw new IllegalArgumentException("Adventure description cannot be null");
        }
    }

    @Override
    public AdventureLorebookEntryDetails execute(CreateAdventureLorebookEntry command) {

        var adventure = repository.findByPublicId(command.adventureId())
                .orElseThrow(() -> new NotFoundException(ADVENTURE_NOT_FOUND));

        var lorebookEntry = adventure.addLorebookEntry(
                command.name(),
                command.description(),
                command.playerId());

        repository.save(adventure);

        var vector = embeddingPort.embed(lorebookEntry.getDescription());
        vectorSearchPort.upsert(adventure.getPublicId(), lorebookEntry.getPublicId(), vector);

        return mapResult(adventure, lorebookEntry);
    }

    private AdventureLorebookEntryDetails mapResult(Adventure adventure, AdventureLorebookEntry entry) {

        return new AdventureLorebookEntryDetails(
                entry.getPublicId(),
                adventure.getPublicId(),
                entry.getName(),
                entry.getDescription(),
                entry.getPlayerId(),
                entry.isPlayerCharacter(),
                entry.getCreationDate(),
                entry.getLastUpdateDate());
    }
}
