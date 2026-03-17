package me.moirai.storyengine.core.application.usecase.adventure;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureBumpByChannelId;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.domain.adventure.Adventure;

@UseCaseHandler
public class UpdateAdventureBumpByChannelIdHandler
        extends AbstractUseCaseHandler<UpdateAdventureBumpByChannelId, Void> {

    private static final String USER_NO_PERMISSION = "User does not have permission to update adventure";
    private static final String ADVENTURE_NOT_FOUND = "Adventure to be updated was not found";

    private AdventureRepository repository;

    public UpdateAdventureBumpByChannelIdHandler(AdventureRepository repository) {
        this.repository = repository;
    }

    @Override
    public Void execute(UpdateAdventureBumpByChannelId useCase) {

        Adventure adventure = repository.findByChannelId(useCase.getChannelId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_NOT_FOUND));

        // TODO externalize to authorizer
        if (!adventure.canUserWrite(useCase.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION);
        }

        repository.updateBumpByChannelId(useCase.getBump(), useCase.getBumpFrequency(), useCase.getChannelId());
        return null;
    }
}
