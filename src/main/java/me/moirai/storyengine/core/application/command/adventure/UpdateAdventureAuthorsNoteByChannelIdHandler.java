package me.moirai.storyengine.core.application.command.adventure;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureAuthorsNoteByChannelId;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@UseCaseHandler
public class UpdateAdventureAuthorsNoteByChannelIdHandler
        extends AbstractUseCaseHandler<UpdateAdventureAuthorsNoteByChannelId, Void> {

    private static final String ADVENTURE_NOT_FOUND = "Adventure to be updated was not found";

    private AdventureRepository repository;

    public UpdateAdventureAuthorsNoteByChannelIdHandler(AdventureRepository repository) {
        this.repository = repository;
    }

    @Override
    public Void execute(UpdateAdventureAuthorsNoteByChannelId useCase) {

        repository.findByChannelId(useCase.getChannelId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_NOT_FOUND));

        repository.updateAuthorsNoteByChannelId(useCase.getAuthorsNote(), useCase.getChannelId());
        return null;
    }
}
