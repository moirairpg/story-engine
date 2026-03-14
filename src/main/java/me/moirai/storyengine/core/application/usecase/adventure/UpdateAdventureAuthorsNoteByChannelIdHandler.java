package me.moirai.storyengine.core.application.usecase.adventure;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.application.usecase.adventure.request.UpdateAdventureAuthorsNoteByChannelId;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureRepository;

@UseCaseHandler
public class UpdateAdventureAuthorsNoteByChannelIdHandler
        extends AbstractUseCaseHandler<UpdateAdventureAuthorsNoteByChannelId, Void> {

    private static final String USER_NO_PERMISSION = "User does not have permission to update adventure";
    private static final String ADVENTURE_NOT_FOUND = "Adventure to be updated was not found";

    private AdventureRepository repository;

    public UpdateAdventureAuthorsNoteByChannelIdHandler(AdventureRepository repository) {
        this.repository = repository;
    }

    @Override
    public Void execute(UpdateAdventureAuthorsNoteByChannelId useCase) {

        Adventure adventure = repository.findByChannelId(useCase.getChannelId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_NOT_FOUND));

        if (!adventure.canUserWrite(useCase.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION);
        }

        repository.updateAuthorsNoteByChannelId(useCase.getAuthorsNote(), useCase.getChannelId());
        return null;
    }
}
