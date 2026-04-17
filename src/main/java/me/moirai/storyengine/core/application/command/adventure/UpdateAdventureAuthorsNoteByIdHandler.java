package me.moirai.storyengine.core.application.command.adventure;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureAuthorsNoteById;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@CommandHandler
public class UpdateAdventureAuthorsNoteByIdHandler
        extends AbstractCommandHandler<UpdateAdventureAuthorsNoteById, Void> {

    private static final String ADVENTURE_NOT_FOUND = "Adventure to be updated was not found";

    private AdventureRepository repository;

    public UpdateAdventureAuthorsNoteByIdHandler(AdventureRepository repository) {
        this.repository = repository;
    }

    @Override
    public Void execute(UpdateAdventureAuthorsNoteById useCase) {

        repository.findByPublicId(useCase.adventureId())
                .orElseThrow(() -> new NotFoundException(ADVENTURE_NOT_FOUND));

        repository.updateAuthorsNoteByPublicId(useCase.authorsNote(), useCase.adventureId());

        return null;
    }
}
