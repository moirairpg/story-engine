package me.moirai.storyengine.core.application.usecase.adventure;

import org.apache.commons.lang3.StringUtils;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.domain.adventure.Adventure;

@UseCaseHandler
public class DeleteAdventureHandler extends AbstractUseCaseHandler<DeleteAdventure, Void> {

    private static final String USER_NO_PERMISSION = "User does not have permission to delete adventure";
    private static final String ADVENTURE_NOT_FOUND = "Adventure to be deleted was not found";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "Adventure ID cannot be null or empty";

    private final AdventureRepository repository;

    public DeleteAdventureHandler(AdventureRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(DeleteAdventure command) {

        if (StringUtils.isBlank(command.getId())) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public Void execute(DeleteAdventure command) {

        Adventure adventure = repository.findById(command.getId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_NOT_FOUND));

        if (!adventure.canUserWrite(command.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION);
        }

        repository.deleteById(command.getId());

        return null;
    }
}
