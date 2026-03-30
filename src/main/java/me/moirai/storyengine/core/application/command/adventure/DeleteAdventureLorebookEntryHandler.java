package me.moirai.storyengine.core.application.command.adventure;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventureLorebookEntry;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.vectorsearch.LorebookVectorSearchPort;

@CommandHandler
public class DeleteAdventureLorebookEntryHandler extends AbstractCommandHandler<DeleteAdventureLorebookEntry, Void> {

    private static final String ENTRY_ID_CANNOT_BE_NULL_OR_EMPTY = "Lorebook entry ID cannot be null or empty";
    private static final String ADVENTURE_ID_CANNOT_BE_NULL_OR_EMPTY = "Adventure ID cannot be null or empty";
    private static final String ADVENTURE_TO_BE_UPDATED_WAS_NOT_FOUND = "Adventure to be updated was not found";

    private final AdventureRepository repository;
    private final LorebookVectorSearchPort vectorSearchPort;

    public DeleteAdventureLorebookEntryHandler(
            AdventureRepository repository,
            LorebookVectorSearchPort vectorSearchPort) {

        this.repository = repository;
        this.vectorSearchPort = vectorSearchPort;
    }

    @Override
    public void validate(DeleteAdventureLorebookEntry command) {

        if (command.entryId() == null) {
            throw new IllegalArgumentException(ENTRY_ID_CANNOT_BE_NULL_OR_EMPTY);
        }

        if (command.adventureId() == null) {
            throw new IllegalArgumentException(ADVENTURE_ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public Void execute(DeleteAdventureLorebookEntry command) {

        var adventure = repository.findByPublicId(command.adventureId())
                .orElseThrow(() -> new NotFoundException(ADVENTURE_TO_BE_UPDATED_WAS_NOT_FOUND));

        adventure.removeLorebookEntry(command.entryId());
        repository.save(adventure);

        vectorSearchPort.delete(command.entryId());

        return null;
    }
}
